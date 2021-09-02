import { DailyNotes } from './DailyNotes';
import { BADGE, Storybook } from '../../storybook';

export default {
  title: 'Routes/DailyNotes',
  component: DailyNotes,
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV]
  },
};

const Template = (args) => <DailyNotes {...args} />;

export const Basic = Template.bind({});
Basic.args = {
  children: 'DailyNotes',
};
