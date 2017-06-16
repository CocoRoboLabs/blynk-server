import React from 'react';
import {Row, Col, Icon, Popconfirm, Button} from 'antd';
import FormItem from 'components/FormItem';
import Preview from 'scenes/Products/components/Preview';
import {SortableHandle} from 'react-sortable-hoc';
import {MetadataSelect} from 'components/Form';
import {MetadataRoles} from 'services/Roles';
import classnames from 'classnames';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {reduxForm, touch, Form, getFormSyncErrors} from 'redux-form';
const DragHandler = SortableHandle(() => <Icon type="bars" className="cursor-move"/>);
import Static from './static';
import _ from 'lodash';

@connect((state, ownProps) => ({
  events: state.Product.edit.events.fields,
  fieldsErrors: getFormSyncErrors(ownProps.form)(state)
}), (dispatch) => ({
  touchFormById: bindActionCreators(touch, dispatch)
}))
@reduxForm({
  touchOnChange: true
})
class MetadataItem extends React.PureComponent {

  static propTypes = {
    events: React.PropTypes.any,
    anyTouched: React.PropTypes.bool,
    invalid: React.PropTypes.bool,
    preview: React.PropTypes.object,
    fieldsErrors: React.PropTypes.any,
    form: React.PropTypes.string,
    fields: React.PropTypes.object,
    children: React.PropTypes.any,
    onDelete: React.PropTypes.func,
    touchFormById: React.PropTypes.func,
    onClone: React.PropTypes.func,
    id: React.PropTypes.number,
    onChange: React.PropTypes.func,
    field: React.PropTypes.object,
    touched: React.PropTypes.bool,
    tools: React.PropTypes.bool,
    updateMetadataFieldInvalidFlag: React.PropTypes.func,
  };

  constructor(props) {
    super(props);

    this.invalid = false;

    this.state = {
      isActive: false
    };
  }

  componentWillMount() {
    if (this.props.field.values.isSavedBefore) {
      this.props.touchFormById(this.props.form, ...Object.keys(this.props.field.values));
    }
  }

  componentWillReceiveProps(props) {
    if (this.invalid !== props.invalid) {
      this.props.onChange({
        ...props.field,
        invalid: props.invalid
      });
      this.invalid = props.invalid;
    }
  }

  shouldComponentUpdate(nextProps, nextState) {
    return !(_.isEqual(this.props.fieldsErrors, nextProps.fieldsErrors)) || !(_.isEqual(this.props.fields, nextProps.fields)) || !(_.isEqual(this.state, nextState)) || !(_.isEqual(this.props.events, nextProps.events));
  }

  handleConfirmDelete() {
    if (this.props.onDelete)
      this.props.onDelete();
  }

  handleCancelDelete() {
    this.setState({isActive: false});
  }

  markAsActive() {
    this.setState({isActive: true});
  }

  preview() {

    const name = this.props.preview.name && this.props.preview.name.trim();

    if (!this.props.anyTouched && !name) {
      return null;
    }

    if (this.props.invalid && !name) {
      return (<Preview> <Preview.Unavailable /> </Preview>);
    }

    return (
      <Preview inline={this.props.preview.inline}>
        <Preview.Name>{name}</Preview.Name>
        <Preview.Value>{this.props.preview.value || 'Empty'}</Preview.Value>
      </Preview>
    );

  }

  handleSubmit() {
    this.props.touchFormById(this.props.form, ...Object.keys(this.props.fields));
  }

  render() {

    let deleteButton;
    if (this.props.anyTouched) {
      deleteButton = (<Popconfirm title="Are you sure?" overlayClassName="danger"
                                  onConfirm={this.handleConfirmDelete.bind(this)}
                                  onCancel={this.handleCancelDelete.bind(this)} okText="Yes, Delete"
                                  cancelText="Cancel">
        <Button icon="delete" size="small" onClick={this.markAsActive.bind(this)}/>
      </Popconfirm>);
    } else {
      deleteButton = (<Button size="small" icon="delete" onClick={this.handleConfirmDelete.bind(this)}/>);
    }

    const itemClasses = classnames({
      'product-metadata-item': true,
      'product-metadata-item-active': this.state.isActive,
    });

    return (
      <div className={itemClasses}>
        <Form onSubmit={this.handleSubmit.bind(this)}>
          <Row gutter={8}>
            <Col span={12}>
              { this.props.children }
            </Col>
            <Col span={3}>
              <FormItem offset={false}>
                <FormItem.Title>Who can edit</FormItem.Title>
                <FormItem.Content>
                  <MetadataSelect name="role" style={{width: '100%'}} values={MetadataRoles}/>
                </FormItem.Content>
              </FormItem>
            </Col>
            <Col span={8}>
              { this.preview() }
            </Col>
          </Row>
          { this.props.tools && (
            <div className="product-metadata-item-tools">
              <DragHandler/>
              {deleteButton}
              <Button icon="copy" size="small" onClick={this.props.onClone.bind(this)}/>
            </div>
          )}
        </Form>
      </div>
    );
  }
}

MetadataItem.Static = Static;
export default MetadataItem;
