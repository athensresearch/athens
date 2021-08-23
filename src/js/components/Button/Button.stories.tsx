import React from 'react';
import styled from 'styled-components';

import { Button } from './Button';

export default {
  title: 'Example/Button',
  component: Button,
  argTypes: {},
};

const Template = (args) => <Button {...args} />;

export const Primary = Template.bind({});
Primary.args = {
  children: 'Button',
};

export const isPrimary = Template.bind({});
isPrimary.args = {
  isPrimary: true,
  children: 'Button',
};

export const Pressed = Template.bind({});
Pressed.args = {
  "aria-pressed": true,
  children: 'Button',
};

export const Icon = Template.bind({});
Icon.args = {
  children: 
  <svg width="24" height="24" stroke-width="1.5" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M9 12H12M15 12H12M12 12V9M12 12V15" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"/>
    <path d="M21 3.6V20.4C21 20.7314 20.7314 21 20.4 21H3.6C3.26863 21 3 20.7314 3 20.4V3.6C3 3.26863 3.26863 3 3.6 3H20.4C20.7314 3 21 3.26863 21 3.6Z" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"/>
  </svg>,
};
