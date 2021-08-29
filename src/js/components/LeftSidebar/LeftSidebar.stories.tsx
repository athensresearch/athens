import React from 'react';
import styled from 'styled-components';
import { classnames } from '../util/classnames';

import { LeftSidebar } from './LeftSidebar';

export default {
  title: 'Components/LeftSidebar',
  component: LeftSidebar,
  argTypes: {},
};

const Template = (args) => <LeftSidebar {...args} />;

export const Primary = Template.bind({});
Primary.args = {
  isLeftSidebarOpen: true,
  shortcuts: [{ uid: '1', title: 'James and the giant', order: 0 }],
  version: '1.0.0'
};
Primary.properties = {
  layout: 'fullscreen'
}

