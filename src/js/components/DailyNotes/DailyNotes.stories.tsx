import { DailyNotes } from './DailyNotes';

export default {
  title: 'Routes/DailyNotes',
  component: DailyNotes,
  argTypes: {},
};

const Template = (args) => <DailyNotes {...args} />;

export const Basic = Template.bind({});
Basic.args = {
  children: 'DailyNotes',
};
