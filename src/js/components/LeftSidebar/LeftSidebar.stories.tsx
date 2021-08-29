import styled from 'styled-components';

import { LeftSidebar } from './LeftSidebar';
import { AppLayout } from '../AppLayout';

export default {
  title: 'Components/LeftSidebar',
  component: LeftSidebar,
  argTypes: {},
};

const Template = (args) => <AppLayout style={{ minHeight: "60vh" }}>
  <LeftSidebar {...args} />
</AppLayout>;

export const Primary = Template.bind({});
Primary.args = {
  isLeftSidebarOpen: true,
  shortcuts: [{ uid: '1', title: 'James and the giant', order: 0 }],
  version: '1.0.0'
};

export const ManyShortcuts = Template.bind({});
ManyShortcuts.args = {
  isLeftSidebarOpen: true,
  shortcuts: [
    { uid: '1', title: 'James and the giant', order: 0 },
    { uid: '2', title: 'James and the giant', order: 0 },
    { uid: '3', title: 'James and the giant', order: 0 },
    { uid: '4', title: 'James and the giant', order: 0 },
    { uid: '5', title: 'James and the giant', order: 0 },
    { uid: '6', title: 'James and the giant', order: 0 },
    { uid: '7', title: 'James and the giant', order: 0 },
    { uid: '8', title: 'James and the giant', order: 0 },
    { uid: '9', title: 'James and the giant', order: 0 },
    { uid: '10', title: 'James and the giant', order: 0 },
    { uid: '11', title: 'James and the giant', order: 0 },
    { uid: '12', title: 'James and the giant', order: 0 },
    { uid: '13', title: 'James and the giant', order: 0 },
    { uid: '14', title: 'James and the giant', order: 0 },
    { uid: '15', title: 'James and the giant', order: 0 },
    { uid: '16', title: 'James and the giant', order: 0 },
    { uid: '17', title: 'James and the giant', order: 0 },
    { uid: '18', title: 'James and the giant', order: 0 },
    { uid: '19', title: 'James and the giant', order: 0 },
    { uid: '20', title: 'James and the giant', order: 0 },
    { uid: '21', title: 'James and the giant', order: 0 },
    { uid: '22', title: 'James and the giant', order: 0 },
    { uid: '23', title: 'James and the giant', order: 0 },
    { uid: '24', title: 'James and the giant', order: 0 },
  ],
  version: '1.0.0'
};

