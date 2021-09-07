import { DailyNotes } from './DailyNotes';
import { DailyNotes as DailyNotesConcept1 } from '../DailyNotes/Next/DailyNotes';
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

export const CalendarSidebar = () => {
  return <DailyNotesConcept1 />;
};
CalendarSidebar.story = {
  parameters: {
    badges: [BADGE.CONCEPT]
  },
};