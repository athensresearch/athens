
import { BADGE, Storybook } from '../../../../storybook';
import * as mockData from './mockData';

import { DBMenu } from './DBMenu';

export default {
  title: 'Components/DBMenu',
  component: DBMenu,
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

export const Default = (args) => <DBMenu
  activeDb={mockData.activeDb}
  inactiveDbs={mockData.inactiveDbs}
  synced={args.synced}
/>;

export const LongDBNames = (args) => <DBMenu
  activeDb={mockData.examples["long db names"].activeDb}
  inactiveDbs={mockData.examples["long db names"].inactiveDbs}
  synced={args.synced}
/>;

export const NoOtherDBs = (args) => <DBMenu
  activeDb={mockData.examples["no other dbs"].activeDb}
  inactiveDbs={mockData.examples["no other dbs"].inactiveDbs}
  synced={args.synced}
/>;

export const LotsOfDBs = (args) => <DBMenu
  activeDb={mockData.examples["lots of dbs"].activeDb}
  inactiveDbs={mockData.examples["lots of dbs"].inactiveDbs}
  synced={args.synced}
/>;
