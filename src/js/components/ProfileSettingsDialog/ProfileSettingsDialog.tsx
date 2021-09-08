import React from 'react';
import styled, { keyframes } from 'styled-components';
import { readableColor } from 'polished';
import { HexColorPicker } from "react-colorful";
import { Fade, Grow } from '@material-ui/core';

import { Button } from '../Button';
import { Avatar } from '../Avatar';
import { Input } from '../Input';
import { Dialog } from '../Dialog';

const ProfileWrap = styled(Dialog.Body)`
  h3 {
    text-align: center;
    margin: 0;
    font-weight: 600;
  }

  hr {
    margin: 1rem 0;
    border: 0;
    border-top: 1px solid var(--border-color);
  }
`;

const Actions = styled(Dialog.Actions)`
  padding-Bottom: 1rem;
  justify-content: center;
  gap: 1rem;

  button {
    width: 5em;
  }
`;

const AvatarWrap = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20rem;
  margin: 1em auto 2em;
  border-radius: 1rem;
  padding: 1rem;
  border: 1px solid var(--border-color);
  background: var(--background-plus-2);

  > * {
    filter: drop-shadow(0 0.25rem 0.25rem var(--shadow-color---opacity-10));
  }
`;

const pulse = keyframes`
  from {
    transform: translate(-50%, -50%) scale(1) ;
  } to {
    transform: translate(-50%, -50%) scale(1.08);
  }
`;

const ColorPickerWrap = styled.div`
  .react-colorful {
    width: 8.5rem;
    height: 3rem;
    gap: 1rem;
    margin: -0.25rem 0 1rem;
    flex-direction: row;

    > * {
      border-radius: 0.5rem;
      height: 100%;
      flex: 0 0 4rem;
    }
  }

  .react-colorful__saturation {
    border-bottom: 0;
  }

  .react-colorful__interactive:focus
  .react-colorful__pointer {
    animation: ${pulse} 0.5s infinite alternate ease-in-out;
  }
`;

const Inputs = styled.div`
  display: flex;
  gap: 2rem;
  align-items: flex-start;
  justify-content: center;
`;

const LabelWrapper = styled(Input.LabelWrapper)`
  gap: 0.25rem;
`;


interface ProfileSettingsDialogProps {
  person: Person;
  isOpen: boolean;
  handleClose: () => void;
  handleUpdatePerson: (person: Person) => void;
}
/**
 * Dialog for modifying the current user's username and color
 */
export const ProfileSettingsDialog = ({
  person,
  handleClose,
  handleUpdatePerson,
  isOpen
}: ProfileSettingsDialogProps) => {
  const [editingUsername, setEditingUsername] = React.useState<string>(person.username || '');
  const [editingColor, setEditingColor] = React.useState<string>(person.color || '#0071DB');
  const [isValidUsername, setIsValidUsername] = React.useState<boolean>(!!editingUsername);

  const handleChangeUsername = (e: React.ChangeEvent<HTMLInputElement>) => {
    const attempt = e.target.value.trim();
    setEditingUsername(attempt);
    setIsValidUsername(attempt.length > 1);
  }

  return (
    <Fade>
    <Dialog
      isDialogOpen={isOpen}
      handleClose={handleClose}
    >
      <Dialog.Header><Dialog.CloseButton onClick={handleClose} /></Dialog.Header>
      <ProfileWrap>
        <h3>How you appear to others</h3>
        <AvatarWrap className={isValidUsername ? "is-valid" : 'is-invalid'}>
          <Avatar
            size="2em"
            personId={person.personId}
            username={isValidUsername ? editingUsername : person.username}
            color={editingColor}
            showTooltip={false}
          />
          <Avatar.Fullname style={{
            "--tooltip-background-color": editingColor,
            "--tooltip-text-color": readableColor(editingColor)
          }}>
            {isValidUsername ? editingUsername : person.username}
          </Avatar.Fullname>
        </AvatarWrap>
        <Inputs>
          <ColorPickerWrap>
            <HexColorPicker color={editingColor} onChange={setEditingColor} />
          </ColorPickerWrap>
          <LabelWrapper>
            <Input required className={!isValidUsername ? 'is-invalid' : ''} type="name" defaultValue={editingUsername} onChange={handleChangeUsername} />
            <Input.Help style={{ opacity: isValidUsername ? 0 : 1 }}>At least 2 characters</Input.Help>
          </LabelWrapper>
        </Inputs>
        <hr />
        <Actions>
          <Button
            shape="round"
            variant="gray"
            onClick={handleClose}
          >
            Cancel
          </Button>
          <Button shape="round" variant="filled"
            disabled={!isValidUsername}
            onClick={() => handleUpdatePerson({ ...person, username: editingUsername, color: editingColor })}
          >
            Save
          </Button>
        </Actions>
      </ProfileWrap>
      </Dialog>
    </Fade>
  )
}
