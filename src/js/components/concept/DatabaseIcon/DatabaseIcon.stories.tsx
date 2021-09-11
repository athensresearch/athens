
import { BADGE, Storybook } from '../../../storybook';

import { DatabaseIcon } from './DatabaseIcon';

export default {
  title: 'Components/DatabaseIcon',
  component: DatabaseIcon,
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV, BADGE.IN_USE]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

const Template = (args) => <DatabaseIcon
  {...args} />;

export const Default = Template.bind({});
Default.args = {
};
