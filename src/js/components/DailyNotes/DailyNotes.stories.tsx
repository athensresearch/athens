import { DailyNotes } from './DailyNotes';

export default {
  title: 'Components/DailyNotes',
  component: DailyNotes,
  argTypes: {},
};

const Template = (args) => <DailyNotes {...args} />;

export const Primary = Template.bind({});
Primary.args = {
  children: 'DailyNotes',
};
