import { DetailPopover } from './DetailPopover';
import { BADGE, Storybook } from '@/utils/storybook';

export default {
  title: 'components/DetailPopover',
  component: DetailPopover,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

export const Typical = () => <DetailPopover
// dbid="dbid"
// uid="uid"
// order={4}
// open={true}
// refs={10}
/>;
