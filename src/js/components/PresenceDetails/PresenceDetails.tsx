import styled from 'styled-components'
import React from 'react';

import { DOMRoot } from '../../config';
import { Wifi } from '@material-ui/icons'
import { Popper, Modal, Fade, PopperPlacementType } from '@material-ui/core'

import { Button } from '../Button';
import { Menu } from '../Menu';
import { Overlay } from '../Overlay';
import { Avatar } from '../Avatar';

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

export interface PresenceDetailsProps {
  hostAddress: HostAddress
  currentUser: Person
  placement?: PopperPlacementType
  currentPageMembers: Person[]
  differentPageMembers: Person[]
  handleUpdateProfile(person: Person): void
  handlePressHostAddress(hostAddress: HostAddress): void
  handlePressMember(person: Person): void
}

export const PresenceDetails = ({
  hostAddress,
  currentPageMembers,
  differentPageMembers,
  handlePressHostAddress,
  handlePressMember,
}: PresenceDetailsProps) => {
  const [isPresenceDetailsOpen, setIsPresenceDetailsOpen] = React.useState(false);
  const [presenceDetailsAnchor, setPresenceDetailsAnchor] = React.useState(null);

  const maxToDisplay = 5;
  const showablePersons = [...currentPageMembers, ...differentPageMembers];

  return (
    <><div ref={setPresenceDetailsAnchor}>
      <Button
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
    </div>
      <Modal
        container={DOMRoot}
        open={isPresenceDetailsOpen}
        BackdropProps={{ invisible: true }}
        onClose={() => setIsPresenceDetailsOpen(false)}
      >
        <Popper
          open={isPresenceDetailsOpen}
          placement="bottom-end"
          disablePortal={true}
          anchorEl={presenceDetailsAnchor}
          transition>
          {({ TransitionProps }) => (
            <Fade {...TransitionProps} timeout={250}>
              <PresenceOverlay className="animate-in">
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
    </>
  );
};


