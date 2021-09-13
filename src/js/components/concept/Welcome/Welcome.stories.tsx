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
    children: 'Welcome',
    databases: mockDatabases.slice(0, 3),
};

export const ManyDatabases = Template.bind({});
ManyDatabases.args = {
    children: 'Welcome',
    databases: mockDatabases
};

export const FirstRun = Template.bind({});
FirstRun.args = {
    children: 'Welcome',
};
