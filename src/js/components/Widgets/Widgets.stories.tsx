import { Widgets } from './Widgets';
import { BADGE, Storybook } from '../../storybook';

export default {
  title: 'concepts/Widgets',
  component: Widgets,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.CONCEPT]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

export const BidirectionalLink = () => (
  <p>
    <Widgets.BidirectionalLink href="#">Abberton Reservoir</Widgets.BidirectionalLink> is a pumped storage freshwater reservoir in England near the Essex coast, with an area of 700 hectares (1,700 acres).
  </p>);