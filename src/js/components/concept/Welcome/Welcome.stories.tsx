import { Welcome } from './Welcome';
import { BADGE, Storybook } from '@/utils/storybook';

import { mockDatabases } from './mockData';

export default {
  title: 'Concepts/Welcome',
  component: Welcome,
  argTypes: {},
  parameters: {
    layout: 'fullscreen',
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <Storybook.Desktop><Story /></Storybook.Desktop>]
};

const Template = (args) => <Welcome {...args} />;

export const Default = Template.bind({});
Default.args = {
  databases: mockDatabases.slice(0, 3),
  currentDatabaseId: mockDatabases[0].id,
};

export const ManyDatabases = Template.bind({});
ManyDatabases.args = {
  databases: mockDatabases,
  currentDatabaseId: mockDatabases[0].id,
};

export const AddDatabase = Template.bind({});
AddDatabase.args = {
  defaultView: 'add-database',
  databases: mockDatabases,
  currentDatabaseId: mockDatabases[0].id,
};

export const JoinDatabase = Template.bind({});
JoinDatabase.args = {
  defaultView: 'join-database',
  databases: mockDatabases,
  currentDatabaseId: mockDatabases[0].id,
};

export const NoRecentDatabases = Template.bind({});
NoRecentDatabases.args = {
  databases: mockDatabases.slice(0, 1),
  currentDatabaseId: mockDatabases[0].id,
};

export const NoDatabases = Template.bind({});
NoDatabases.args = {
  defaultView: 'databases-list'
};

export const FirstRun = Template.bind({});
FirstRun.args = {
};
