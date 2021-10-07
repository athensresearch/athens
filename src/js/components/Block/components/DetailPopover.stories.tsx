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
  block={{
    uid: '1a415560',
    id: 145,
    order: 12,
    open: true,
    _refs: [{ id: 123 }, { id: 123 }]
  }}
/>;
