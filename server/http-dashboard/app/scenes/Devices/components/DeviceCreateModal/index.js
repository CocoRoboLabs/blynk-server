import React from 'react';
import _ from 'lodash';
import {Modal} from 'components';
import {Row, Col, Button} from 'antd';
import {Item, Input} from 'components/UI';
import {MetadataSelect} from 'components/Form';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import Validation from 'services/Validation';
import {reduxForm, getFormSyncErrors, getFormValues, reset, change} from 'redux-form';
import {DeviceCreate, DevicesFetch} from 'data/Devices/api';
import {AVAILABLE_HARDWARE_TYPES, AVAILABLE_CONNECTION_TYPES} from 'services/Devices';
import './styles.less';

@connect((state) => ({
  products: state.Product.products,
  errors: getFormSyncErrors('DeviceCreate')(state),
  formValues: getFormValues('DeviceCreate')(state)
}), (dispatch) => ({
  change: bindActionCreators(change, dispatch),
  resetForm: bindActionCreators(reset, dispatch),
  fetchDevices: bindActionCreators(DevicesFetch, dispatch),
  createDevice: bindActionCreators(DeviceCreate, dispatch)
}))
@reduxForm({
  form: "DeviceCreate"
})
class DeviceCreateModal extends React.Component {

  static propTypes = {
    visible: React.PropTypes.bool,
    onClose: React.PropTypes.func,
    errors: React.PropTypes.object,
    formValues: React.PropTypes.object,
    products: React.PropTypes.array,
    reduxForm: React.PropTypes.func,
    resetForm: React.PropTypes.func,
    createDevice: React.PropTypes.func,
    fetchDevices: React.PropTypes.func,
    change: React.PropTypes.func,
  };

  state = {
    loading: false,
    productId: null
  };

  componentWillReceiveProps(nextProps) {
    if (nextProps && nextProps.formValues && nextProps.formValues.productId !== this.state.productId) {
      this.setState({
        productId: nextProps.formValues.productId
      });

      const product = _.find(this.props.products, (product) => {
        return Number(product.id) === Number(nextProps.formValues.productId);
      });

      this.props.change('boardType', product.boardType);
      this.props.change('connectionType', product.connectionType);
    }
  }

  handleCancelClick() {
    this.props.resetForm('DeviceCreate');
    this.props.onClose();
  }

  handleOkClick() {
    this.setState({
      loading: true
    });
    this.props.createDevice(this.props.formValues).then(() => {
      this.props.fetchDevices().then(() => {
        this.setState({
          loading: false
        });
        this.handleCancelClick();
      });
    });
  }

  render() {

    const products = this.props.products.map((product) => ({
      key: String(product.id),
      value: product.name
    }));

    return (
      <Modal title="New Device"
             visible={this.props.visible}
             confirmLoading={this.state.loading}
             onCancel={this.handleCancelClick.bind(this)}
             footer={[
               <Button key="cancel" type="default" size="default"
                       onClick={this.handleCancelClick.bind(this)}>Cancel</Button>,
               <Button key="save" type="primary" size="default" disabled={!!this.props.errors}
                       loading={this.state.loading}
                       onClick={this.handleOkClick.bind(this)}>
                 Create
               </Button>,
             ]}>
        <div className="device-create-modal">
          <Row>
            <Col span={24}>
              <Item label="Device Name" offset="large">
                <Input name="name" placeholder="New Device" validate={[Validation.Rules.required]}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col span={24}>
              <Item label="Product Template" offset="large">
                <MetadataSelect name="productId" values={products} placeholder="Choose product"
                                validate={[Validation.Rules.required]}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col span={10}>
              <Item label="Hardware" offset="none">
                <MetadataSelect name="boardType" values={AVAILABLE_HARDWARE_TYPES} placeholder="Hardware"
                                validate={[Validation.Rules.required]}/>
              </Item>
            </Col>
            <Col span={12} offset={2}>
              <Item label="Connection Type" offset="none">
                <MetadataSelect name="connectionType" values={AVAILABLE_CONNECTION_TYPES} placeholder="Choose product"
                                validate={[Validation.Rules.required]}/>
              </Item>
            </Col>
          </Row>
        </div>
      </Modal>
    );
  }

}

export default DeviceCreateModal;
