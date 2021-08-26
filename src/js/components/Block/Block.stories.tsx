import { Block } from './Block';

export default {
  title: 'Example/Block',
  component: Block,
  argTypes: {},
};

const Template = (args) => <Block {...args} />;

export const Primary = Template.bind({});
Primary.args = {
  isOpen: true,
};

export const Typical = Template.bind({});
Typical.args = {
  content: 'Block',
  isOpen: true,
};
