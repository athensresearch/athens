import styled from 'styled-components'
import React from 'react';

import { DOMRoot } from '../../../config';
import { Wifi, Settings } from '@material-ui/icons'
import { Popper, Modal, Fade } from '@material-ui/core'

import { Button } from '../../Button';
import { Menu } from '../../Menu';
import { Overlay } from '../../Overlay';
import { Avatar } from '../../Avatar';
import { Input } from '../../Input';
import { Dialog } from '../../Dialog';



const ColorOptions = ["#0071DB", "#F9A132", "#009E23"];

const MemberCount = styled.span`
  margin-left: 0.125rem;
`;

const Heading = styled.h3`
  margin: 0;
  padding: 0.25rem 0.5rem;
  font-size: var(--font-size--text-xs);
  color: var(--body-text-color---opacity-med);
`;

const PresenceOverlay = styled(Overlay)`
  min-width: 12em;
`;

const HostIcon = styled(Wifi)`
  padding: 0.25rem;
  background: var(--background-minus-2);
  border-radius: 100em;
`;

export const PresenceDetails = ({
  hostAddress,
  currentUser,
  currentPageMembers,
  handlePressSelf,
  differentPageMembers,
  handlePressHostAddress,
  handlePressMember,
  placement,
  isUserSettingsDialogOpen,
  setCurrentUserUsername,
  setIsUserSettingsDialogOpen,
  setCurrentUserColor,
  currentUserUsername,
  currentUserColor,
}) => {
  const [isPresenceDetailsOpen, setIsPresenceDetailsOpen] = React.useState(false);
  const [presenceDetailsAnchor, setPresenceDetailsAnchor] = React.useState(null);

  const maxToDisplay = 5;
  const showablePersons = [...currentPageMembers, ...differentPageMembers];

  return (
    <>
      <Button
        ref={setPresenceDetailsAnchor}
        onClick={() => setIsPresenceDetailsOpen(!isPresenceDetailsOpen)}
        style={{ borderRadius: "100em" }}
        isPressed={isPresenceDetailsOpen}>
        <Avatar.Stack
          size="1em"
          maskSize="1.5px"
          overlap="0.2"
        >
          {showablePersons && showablePersons.slice(0, maxToDisplay).map((member, index) => {
            if (index < maxToDisplay) {
              return (
                <Avatar
                  key={member.personId}
                  username={member.username}
                  color={member.color}
                  personId={member.personId}
                  showTooltip={false}
                />
              );
            }
            return null;
          })}
        </Avatar.Stack>
        {showablePersons.length > maxToDisplay && <MemberCount>+{showablePersons.length - maxToDisplay}</MemberCount>}
      </Button>
      <Modal
        container={DOMRoot}
        open={isPresenceDetailsOpen}
        BackdropProps={{ invisible: true }}
        onClose={() => setIsPresenceDetailsOpen(false)}
      >
        <Popper
          open={isPresenceDetailsOpen}
          placement={placement ? placement : 'bottom-end'}
          disablePortal={true}
          anchorEl={presenceDetailsAnchor}
          transition>
          {({ TransitionProps }) => (
            <Fade {...TransitionProps} timeout={250}>
              <PresenceOverlay className="animate-in">
                <Button onClick={() => handlePressSelf()} key={currentUser.personId}>
                  <Avatar
                    username={currentUser.username}
                    personId={currentUser.personId}
                    color={currentUser.color}
                    showTooltip={false}
                  />
                  <span>{currentUser.username}</span>
                  <Settings />
                </Button>

                {hostAddress && <><Button onClick={handlePressHostAddress}><HostIcon /> <span>{hostAddress}</span></Button>
                  <Menu.Separator /></>
                }
                {currentPageMembers.length > 0 && (
                  <>
                    <Heading>On this page</Heading>
                    <Menu>
                      {currentPageMembers.length > 0 && currentPageMembers.map(member =>
                        <Button onClick={(member) => handlePressMember(member)} key={member.personId}>
                          <Avatar
                            username={member.username}
                            personId={member.personId}
                            color={member.color}
                            showTooltip={false}
                          />
                          <span>{member.username}</span>
                        </Button>
                      )}
                    </Menu>
                    <Menu.Separator />
                  </>
                )}
                <Menu>
                  {differentPageMembers.length > 0 && differentPageMembers.map(member =>
                    <Button onClick={(member) => handlePressMember(member)} key={member.personId}>
                      <Avatar
                        username={member.username}
                        personId={member.personId}
                        color={member.color}
                        showTooltip={false}
                        isMuted={true}
                      />
                      <span>{member.username}</span>
                    </Button>
                  )}
                </Menu>
              </PresenceOverlay>
            </Fade>
          )}
        </Popper>
      </Modal>

      {isUserSettingsDialogOpen && <Dialog
        handleClose={() => setIsUserSettingsDialogOpen(false)}
      >
        <Dialog.Header>
          <Dialog.Title>You</Dialog.Title>
          <Dialog.CloseButton onClick={() => setIsUserSettingsDialogOpen(false)} />
        </Dialog.Header>
        <Dialog.Body>
          <div>
            <Avatar personId="123" username={currentUserUsername} color={currentUserColor} />
            {ColorOptions.map((color) => <Button key={color} onClick={() => setCurrentUserColor(color)}>{color}</Button>)}
          </div>
          <div>
            <Input type="name" defaultValue={currentUserUsername} onChange={e => setCurrentUserUsername(e.target.value)} />
          </div>
        </Dialog.Body>
        <Dialog.Actions>

        </Dialog.Actions>
      </Dialog>}
    </>
  );
};


