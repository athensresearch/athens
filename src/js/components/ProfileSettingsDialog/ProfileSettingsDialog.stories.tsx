import React from 'react';

import { BADGE, Storybook } from '../../storybook';
import { ProfileSettingsDialog } from './ProfileSettingsDialog';

import { Button } from '../Button';

export default {
  title: 'Components/ProfileSettingsDialog',
  component: ProfileSettingsDialog,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.CONCEPT]
  }
};

const testPerson = { personId: '123', username: 'John Doe', color: '#0071ed' };

const Template = (args, context) => {
  const [isDialogOpen, setIsDialogOpen] = React.useState(true);

  const handleUpdatePerson = React.useCallback((person) => {
    console.log('Person updated', person);
    setIsDialogOpen(false);
  }, []);

  return (
    <>
      <Button
        shape="round"
        variant="filled"
        onClick={() => setIsDialogOpen(true)}>
        Open
      </Button>
      <Storybook.Wrapper>
        <ProfileSettingsDialog
          person={testPerson}
          isOpen={isDialogOpen}
          handleUpdatePerson={handleUpdatePerson}
          handleClose={() => setIsDialogOpen(false)}
          {...args}
        />
      </Storybook.Wrapper>
    </>)
};

export const Default = Template.bind({});