import React from 'react';
import {Row, Col} from 'antd';
import {Fieldset, DeviceStatus, DeviceAuthToken, Section, DeviceMetadata} from 'components';
import _ from 'lodash';
import './styles.less';

class DeviceInfo extends React.Component {

  static propTypes = {
    device: React.PropTypes.object,
    onChange: React.PropTypes.func
  };

  shouldComponentUpdate(nextProps) {
    return !(_.isEqual(nextProps.device, this.props.device));
  }

  getDeviceStatus() {
    if (this.props.device && this.props.device.get('status') === 'OFFLINE') {
      return 'offline';
    } else if (this.props.device && this.props.device.get('status') === 'ONLINE') {
      return 'online';
    }
  }

  onChange(metafield) {

    const device = this.props.device.update('metaFields', (metafields) => metafields.map((value) => {
      if (metafield.get('name') === value.get('name'))
        return metafield;
      return value;
    }));

    return this.props.onChange(device);
  }

  render() {

    return (
      <div className="device--device-info">
        <Row className="device--device-info-details">
          <Col span={8}>
            <Fieldset>
              <Fieldset.Legend>Status</Fieldset.Legend>
              <DeviceStatus status={this.getDeviceStatus()}/>
            </Fieldset>
            <Fieldset>
              <Fieldset.Legend>Auth Token</Fieldset.Legend>
              <DeviceAuthToken authToken={this.props.device.get('token')}/>
            </Fieldset>
          </Col>
          <Col span={8}>
            <Fieldset>
              <Fieldset.Legend>Last Reported</Fieldset.Legend>
              Today, 12:35 AM
            </Fieldset>
            <Fieldset>
              <Fieldset.Legend>Organization</Fieldset.Legend>
              Blynk
            </Fieldset>
          </Col>
          <Col span={8}>
            <div className="device--device-info-logo">
              <img src="http://www.knightequip.com/images/product_warewash_nav/ump-hospitality.jpg"/>
            </div>
          </Col>
        </Row>
        <Row>
          <Col span={24}>
            <Section title="Metadata">
              <div className="device--device-info-metadata-list">
                {
                  this.props.device.get('metaFields').map((field, key) => {

                    const form = `devicemetadataedit${field.get('name')}`;

                    const props = {
                      data: field,
                      key: key,
                      form: form,
                      onChange: this.onChange.bind(this)
                    };

                    if (field.get('type') === 'Text')
                      return (<DeviceMetadata.Text {...props}/>);

                    if (field.get('type') === 'Number')
                      return (<DeviceMetadata.Number {...props}/>);
                  })
                }
              </div>
            </Section>
          </Col>
        </Row>
      </div>
    );
  }

}

export default DeviceInfo;
