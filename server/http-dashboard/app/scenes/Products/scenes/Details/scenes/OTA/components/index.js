import React from 'react';
import {Table, Button, Popconfirm, Row, Col} from 'antd';
import PropTypes from 'prop-types';
import {reduxForm} from 'redux-form';
import {
  Item
} from "components/UI";
import {
  Field as FormField
} from 'components/Form';

import './styles.less';

import DeviceStatus from './components/DeviceStatus';

@reduxForm({
  form: 'OTA'
})
class OTA extends React.Component{

  static propTypes = {
    devices: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.number,
      name: PropTypes.string,
      status: PropTypes.oneOf(['ONLINE', 'OFFLINE']), // use this for column "status" and display like a green / gray dot
      disconnectTime: PropTypes.number, // display "Was online N days ago" when user do mouseover the gray dot (idea is to display last time when device was online if it's offline right now)
      hardwareInfo: PropTypes.shape({
        version: PropTypes.string
      })
    }))
  };

  constructor(props) {
    super(props);

    this.state = {
      'selectedRows': 0,
      'usersDeleteLoading': false,
      'sortedInfo': {
        order: 'ascend',
        columnKey: 'name'
      }
    };
  }

  updateConfirmation() {
    return (
      <div className="devices-ota-update-confirmation">
        <div className="devices-ota-update-confirmation-name">
          <Item label="Firmware Name" offset="medium">
            <FormField name={'firmwareName'} placeholder={'Example: Blynk v1.0.0'}/>
          </Item>
        </div>
        <div className="devices-ota-update-confirmation-file-name">
          FileName.bin
        </div>
        <div className="devices-ota-update-confirmation-fields-list">
          <div className="devices-ota-update-confirmation-fields-list-item">
            Field 1
          </div>
          <div className="devices-ota-update-confirmation-fields-list-item">
            Field 2
          </div>
          <div className="devices-ota-update-confirmation-fields-list-item">
            Field 3
          </div>
        </div>
        <div className="devices-ota-update-confirmation-footer">
          <Row>
            <Col span={12}>
              <div className="devices-ota-update-confirmation-footer-selected-devices-count">
                23 Devices Selected
              </div>
            </Col>
            <Col span={12}>
              <div className="devices-ota-update-confirmation-footer-confirm-btn">
                <Button type="primary">Update firmware</Button>
              </div>
            </Col>
          </Row>
        </div>
      </div>
    );
  }

  updateColumns() {
    return [{
      title: 'Name',
      dataIndex: 'name',
    }, {
      title: 'Status',
      dataIndex: 'status',

      filters: [{
        text: 'Active',
        value: 'Active',
      }, {
        text: 'Pending',
        value: 'Pending',
      }],
      filterMultiple: false,
      onFilter: (value, record) => record.status === value,

      render: (text, record) => <DeviceStatus status={record.status} disconnectTime = {record.disconnectTime}/>
    }, {
      title: 'Firmware version',
      dataIndex: 'hardwareInfo.version',
    }];
  }
  getDataSource(){

    return this.props.devices;
  }
  render(){

    const rowSelection = {
      onChange: (selectedRowKeys, selectedRows) => {
        console.log(`selectedRowKeys: ${selectedRowKeys}`, 'selectedRows: ', selectedRows);
      },
      getCheckboxProps: record => ({
        name: record.name,
      }),
    };
    console.log(this.props);
    const dataSource = this.getDataSource();
    const columns = this.updateColumns();
    return (
      <div className="users-profile--organization-settings--organization-users">

        { this.updateConfirmation() }

        <div className="users-profile--organization-settings--organization-users-delete-button">
          <Popconfirm title="Are you sure you want to delete selected users?"
                      okText="Yes"
                      cancelText="No"
                      overlayClassName="danger">
            <Button type="danger"
                    // disabled={!this.state.selectedRows.length}
                    // loading={this.state.usersDeleteLoading}
            >Update</Button>
          </Popconfirm>
        </div>
        <Table
          rowKey={(record) => record.name}
          rowSelection={rowSelection} columns={columns} dataSource={dataSource}
               pagination={false}/>
      </div>
    );
  }
}

export default OTA;
