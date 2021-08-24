import React from 'react';
import styled from 'styled-components';

import { Block } from './Block';

export default {
  title: 'Example/Block',
  component: Block,
  argTypes: {},
};

const Template = (args) => <Block {...args} />;

export const Primary = Template.bind({});
Primary.args = {
  children: 'Content text',
  hasChildren: true,
  refsCount: 1,
  isOpen: true,
};

export const Pressed = Template.bind({});
Pressed.args = {
  "aria-pressed": true,
  children: 'Block',
};
