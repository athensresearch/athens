import React from 'react';
import { BADGE, Storybook } from '../../storybook';
import * as mockData from './mockData';

import { PresenceDetails } from './PresenceDetails';
import { PresenceDetails as WithUserSettingsDetails } from './Concept/PresenceDetails';

export default {
  title: 'Components/PresenceDetails',
  component: PresenceDetails,
  argTypes: {},
  parameters: {
    // layout: 'centered',
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

export const WithUserSettings = () => {
  const [isUserSettingsDialogOpen, setIsUserSettingsDialogOpen] = React.useState(false);
  const [currentUserColor, setCurrentUserColor] = React.useState('#ff0000');
  const [currentUserUsername, setCurrentUserUsername] = React.useState('Jeff Tang');

  const handlePressSelf = () => {
    setIsUserSettingsDialogOpen(true);
  }

  return (<WithUserSettingsDetails
    hostAddress={mockData.hostAddress}
    currentUser={{ username: currentUserUsername, color: currentUserColor, personId: '123' }}
    handlePressSelf={handlePressSelf}
    currentPageMembers={mockData.currentPageMembers}
    differentPageMembers={mockData.differentPageMembers}
    isUserSettingsDialogOpen={isUserSettingsDialogOpen}
    setIsUserSettingsDialogOpen={setIsUserSettingsDialogOpen}
    currentUserColor={currentUserColor}
    setCurrentUserColor={setCurrentUserColor}
    currentUserUsername={currentUserUsername}
    setCurrentUserUsername={setCurrentUserUsername}
    placement="bottom-start"
  />)
};
WithUserSettings.stories = {
  parameters: {
    badges: [BADGE.DEV]
  }
}