import { Meter } from './Meter';
import { BADGE, Storybook } from '../../../storybook';

export default {
  title: 'Concepts/Meter',
  component: Meter,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.CONCEPT]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

export const Default = (args) => (<Meter {...args} />);