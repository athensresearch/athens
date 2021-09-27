import { Toggle } from './Toggle';
import { BADGE, Storybook } from '@/utils/storybook';

export default {
  title: 'Components/Toggle',
  component: Toggle,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV, BADGE.IN_USE]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

const Template = (args) => <Toggle {...args} />;

export const Default = Template.bind({});
Default.args = {
  defaultSelected: true
};

export const Wide = Template.bind({});
Wide.args = {
  toggleShape: {
    width: 80,
    height: 36,
    inset: 1.5
  }
};

export const Labeled = Template.bind({});
Labeled.args = {
  checkedLabel: 'On',
  unCheckedLabel: 'Off',
  style: { fontSize: '2rem' },
  toggleShape: {
    width: 120,
    height: 50,
    inset: 1.5
  }
};
