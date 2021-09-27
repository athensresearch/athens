import { BADGE, Storybook } from '@/utils/storybook';

import { Spinner } from './Spinner';

export default {
  title: 'Components/Spinner',
  component: Spinner,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV]
  }
};

const Template = (args) => <Storybook.Wrapper><Spinner {...args} /></Storybook.Wrapper>;

export const Basic = Template.bind({});
Basic.args = {
  children: 'Spinner',
};