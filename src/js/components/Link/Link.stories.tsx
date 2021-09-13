import { Link } from './Link';
import { BADGE, Storybook } from '@/utils/storybook';

export default {
  title: 'components/Link',
  component: Link,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

export const BidirectionalLink = () => (
  <p>
    <Link href="#">Abberton Reservoir</Link> is a pumped storage freshwater reservoir in England near the Essex coast, with an area of 700 hectares (1,700 acres).
  </p>);
