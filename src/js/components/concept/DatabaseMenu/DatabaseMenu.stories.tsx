
import { BADGE, Storybook } from '@/utils/storybook';
import { mockDatabases } from './mockData';

import { DatabaseMenu } from './DatabaseMenu';

export default {
  title: 'Concepts/DatabaseMenu',
  component: DatabaseMenu,
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV, BADGE.IN_USE]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

const Template = (args) => <DatabaseMenu
  handleChooseDatabase={() => null}
  handlePressAddDatabase={() => null}
  handlePressRemoveDatabase={() => null}
  handlePressImportDatabase={() => null}
  activeDatabase={mockDatabases[0]}
  synced={true}
  {...args} />;

export const Default = Template.bind({});
Default.args = {
  inactiveDatabases: mockDatabases.slice(1, 4)
};

export const LotsOfDBs = Template.bind({});
LotsOfDBs.args = {
  inactiveDatabases: mockDatabases
};

