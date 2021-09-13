import React from 'react';
import { BADGE, Storybook } from '../../storybook';

import { Button } from '../Button';
import { Dialog } from './Dialog';

export default {
  title: 'components/Dialog',
  component: Dialog,
  argTypes: {},
  parameters: {
    layout: 'fullscreen',
    badges: [BADGE.DEV, BADGE.IN_USE]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

export const DialogWithState = () => {
  const [isDialogOpen, setIsDialogOpen] = React.useState(true);

  return (
    <>
      <Button shape="round" isPrimary onClick={() => setIsDialogOpen(true)}>Open Dialog</Button>
      <Dialog
        isDialogOpen={isDialogOpen}
        handleClose={() => setIsDialogOpen(false)}
      >
        <Dialog.Header>
          <Dialog.Title>Dialog Title</Dialog.Title>
          <Dialog.CloseButton onClick={() => setIsDialogOpen(false)} />
        </Dialog.Header>
        <Dialog.Body>dialog content</Dialog.Body>
        <Dialog.Actions>
          <Button variant="gray">Cancel</Button>
          <Button variant="filled">Save</Button>
        </Dialog.Actions>
      </Dialog>
    </>);
}