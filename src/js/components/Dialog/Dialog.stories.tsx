import { Dialog } from './Dialog';
import { BADGE, Storybook } from '@/utils/storybook';

export default {
  title: 'Components/Dialog',
  component: Dialog,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV, BADGE.IN_USE]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

const Template = (args) => <Dialog {...args} />;

export const Default = Template.bind({});
Default.args = {
  title: 'Lorem ipsum dolor sit amet.',
  children: 'Lorem ipsum dolor sit amet',
  isOpen: true,
};

export const Image = Template.bind({});
Image.args = {
  image: <img src="https://via.placeholder.com/150" />,
  title: 'Lorem ipsum dolor sit amet.',
  children: 'Lorem ipsum dolor sit amet',
  isOpen: true,
};

export const Minimal = Template.bind({});
Minimal.args = {
  title: 'Lorem ipsum dolor sit amet.',
  isOpen: true,
};
