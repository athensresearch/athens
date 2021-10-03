import { Storybook } from '@/utils/storybook';
import { Notification } from './Notification';

export default {
  title: 'Components/Notification',
  component: Notification,
  argTypes: {},
  parameters: {
    layout: 'fullscreen'
  }
};

const Template = (Story, args) => <Storybook.Wrapper><Story {...args} /></Storybook.Wrapper>;

export const Basic = Template.bind({});
Basic.args = {
  children: 'Spinner',
};