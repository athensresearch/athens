import React from 'react';
import { BADGE, Storybook } from '../../storybook';
import * as mockData from './mockData';

import { PresenceDetails } from './PresenceDetails';

export default {
  title: 'Components/PresenceDetails',
  component: PresenceDetails,
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

const Template = (args) => {
  return (<PresenceDetails
    hostAddress={mockData.hostAddress}
    currentPageMembers={mockData.currentPageMembers}
    differentPageMembers={mockData.differentPageMembers}
    {...args} />)
};

export const Basic = Template.bind({});
Basic.args = {}