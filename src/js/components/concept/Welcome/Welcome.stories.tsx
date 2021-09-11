import { Welcome } from './Welcome';
import { BADGE, Storybook } from '../../../storybook';

import { mockDatabases } from './mockData';

export default {
  title: 'Components/Welcome',
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
    children: 'Welcome',
    databases: mockDatabases
};
