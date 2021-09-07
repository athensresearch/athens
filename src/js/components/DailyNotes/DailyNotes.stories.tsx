import { DailyNotes } from './DailyNotes';
import { DailyNotes as DailyNotesSidebarCalendar } from './SidebarCalendar/DailyNotes';
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

export const SidebarCalendar = () => {
  return <DailyNotesSidebarCalendar />;
};
SidebarCalendar.story = {
  parameters: {
    badges: [BADGE.CONCEPT]
  },
};