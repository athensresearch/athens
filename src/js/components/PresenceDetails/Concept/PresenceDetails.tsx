import styled from 'styled-components'
import React from 'react';

import { DOMRoot } from '../../../config';
import { Wifi } from '@material-ui/icons'
import { Popper, Modal, Fade } from '@material-ui/core'

import { Button } from '../../Button';
import { Menu } from '../../Menu';
import { Overlay } from '../../Overlay';
import { Avatar } from '../../Avatar';
import { ProfileSettingsDialog } from '../../ProfileSettingsDialog';


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
  min-width: 14em;
`;

const HostIcon = styled(Wifi)`
  padding: 0.25rem;
  background: var(--background-minus-2);
  border-radius: 100em;
`;

const Profile = styled.div`
  display: flex;
  padding: 0.35rem;
  align-items: center;

  ${Avatar.Wrapper} {
    margin-right: 0.75rem;
  }

  button {
    margin-left: auto;
    font-size: var(--font-size--text-xs);
  }
`;

export interface PresenceDetailsProps {
  handleUpdateProfile(person): void
  hostAddress: string
  currentUser: Person
  currentPageMembers: Person[]
  differentPageMembers: Person[]
}

export const PresenceDetails = ({
  hostAddress,
  currentUser,
  placement,
  currentPageMembers,
  differentPageMembers,
  handlePressHostAddress,
  handlePressMember,
  handleUpdateProfile,
}) => {
  const [isPresenceDetailsOpen, setIsPresenceDetailsOpen] = React.useState(false);
  const [presenceDetailsAnchor, setPresenceDetailsAnchor] = React.useState(null);
  const [isUserSettingsDialogOpen, setIsUserSettingsDialogOpen] = React.useState(false);

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
                <Profile>
                  <Avatar
                    username={currentUser.username}
                    personId={currentUser.personId}
                    color={currentUser.color}
                    showTooltip={false}
                  />
                  <span>{currentUser.username}</span>
                  <Button
                    onClick={() => setIsUserSettingsDialogOpen(true)}
                    key={currentUser.personId}
                  >
                    Edit
                  </Button>
                </Profile>

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

      {isUserSettingsDialogOpen &&
        <ProfileSettingsDialog
          person={{ ...currentUser }}
          isOpen={isUserSettingsDialogOpen}
          handleClose={() => setIsUserSettingsDialogOpen(false)}
          handleUpdatePerson={(person) => {
            handleUpdateProfile(person)
            setIsUserSettingsDialogOpen(false)
          }}
        />}
    </>
  );
};


