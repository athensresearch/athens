import styled, { keyframes } from "styled-components";
import React from "react";

import { Text, Tooltip, Avatar, AvatarGroup, Menu, MenuDivider, MenuButton, MenuList, MenuGroup, MenuItem, Button, Portal } from '@chakra-ui/react';

import { RefreshDouble, Lock } from "iconoir-react";

import { ProfileSettingsDialog } from "@/ProfileSettingsDialog";
import { ConnectedGraphConnection } from "@/Icons/ConnectedGraphConnection";
import { Icon } from "@/Icons/Icon";

const rotate = keyframes`
  from {
    transform: rotate(0deg);
  } to {
    transform: rotate(360deg);
  }
`;

const OfflineIcon = styled(Lock)`
  width: 1.25rem;
  height: 1.25rem;
`;

const ActivityIcon = styled(RefreshDouble)`
  width: 1.25rem;
  height: 1.25rem;
  animation: ${rotate} 2s linear infinite;
  stroke-width: 2;
  vector-effect: non-scaling-stroke;
`;

const ConnectedGraphIconWrap = styled(Icon)`
  --size: 1.5rem;
`;

const connectionStatusIndicator = {
  connected: (
    <ConnectedGraphIconWrap>
      <ConnectedGraphConnection />
    </ConnectedGraphIconWrap>
  ),
  connecting: (
    <>
      <ActivityIcon />
      <span>Connecting...</span>
    </>
  ),
  reconnecting: (
    <>
      <ActivityIcon />
      <span>Reconnecting...</span>
    </>
  ),
  offline: (
    <>
      <OfflineIcon />
      <span>View Only</span>
    </>
  ),
};

const connectionStatusHelpText = {
  connecting: "Athens is connecting to the host.",
  connected: "View connection details.",
  reconnecting: "Athens is attempting to reconnect to the host.",
  offline: "Athens is not connected.",
};

export interface PresenceDetailsProps {
  hostAddress: HostAddress;
  currentUser?: Person;
  currentPageMembers: Person[];
  differentPageMembers: Person[];
  handleUpdateProfile(person: Person): void;
  handleCopyHostAddress(hostAddress: HostAddress): void;
  handlePressMember(person: Person): void;
  connectionStatus: ConnectionStatus;
  defaultOpen?: boolean;
}
interface ConnectionButtonProps {
  connectionStatus: ConnectionStatus;
  showablePersons: Person[];
}

const ConnectionButton = React.forwardRef((props: ConnectionButtonProps, ref) => {
  const { connectionStatus, showablePersons } = props;
  return (
    <Tooltip label={showablePersons.length + " members online"}>
      <Button
        ref={ref as any}
        as={MenuButton}
        bg="transparent"
        borderRadius="full"
        px={1}
        _hover={{
          bg: "background.upper",
        }}
      >
        {connectionStatus === "connected" && (
          showablePersons.length > 0 && (
            <AvatarGroup size="xs" max={5}>
              {showablePersons.map((member) => (
                <Avatar
                  key={member.personId}
                  name={member.username}
                  bg={member.color} />
              ))}
            </AvatarGroup>
          )
        )}
      </Button>
    </Tooltip>
  )
});


export const PresenceDetails = (props: PresenceDetailsProps) => {
  const {
    hostAddress,
    currentUser,
    currentPageMembers,
    differentPageMembers,
    handleCopyHostAddress,
    handlePressMember,
    handleUpdateProfile,
    connectionStatus,
  } = props;
  const showablePersons = [ ...currentPageMembers, ...differentPageMembers ];
  const [ shouldShowProfileSettings, setShouldShowProfileSettings ] = React.useState(false);

  return connectionStatus === "local" ? (
    <></>
  ) : (
    <>
      <Menu placement="bottom-end" size="sm">
        <ConnectionButton
          connectionStatus={connectionStatus}
          showablePersons={showablePersons}
        />
        <Portal>
          <MenuList>
            <>
              {hostAddress && (
                <MenuItem
                  onClick={() => handleCopyHostAddress(hostAddress)}
                  display="flex"
                  flexDirection="column"
                  textAlign="left"
                  justifyContent="flex-start"
                  alignItems="stretch"
                >
                  <Text>Copy address</Text>
                  <Text
                    fontSize="sm"
                    color="foreground.secondary"
                    maxWidth="14em"
                    whiteSpace="nowrap"
                    overflow="hidden"
                    textOverflow="ellipsis"
                  >
                    {hostAddress}
                  </Text>
                </MenuItem>
              )}

              {currentUser && (
                <MenuItem onClick={() => setShouldShowProfileSettings(true)}>Edit appearance</MenuItem>
              )}

              {currentPageMembers.length > 0 && (
                <>
                  <MenuDivider />
                  <MenuGroup title="On this page">
                    {currentPageMembers.map((member) => (
                      <MenuItem
                        onClick={() => handlePressMember(member)}
                        key={member.personId}
                        icon={<Avatar
                          size="xs"
                          marginBlock={-1}
                          name={member.username}
                          bg={member.color}
                        />}
                      >
                        {member.username}
                      </MenuItem>
                    ))}
                  </MenuGroup>
                </>
              )}

              {differentPageMembers.length > 0 && (
                <>
                  <MenuDivider />
                  <MenuGroup title="On other pages">
                    {differentPageMembers.map((member) => (
                      <MenuItem
                        onClick={() => handlePressMember(member)}
                        key={member.personId}
                        icon={<Avatar
                          marginBlock={-1}
                          size="xs"
                          name={member.username}
                          bg={member.color}
                        />}
                      >
                        {member.username}
                      </MenuItem>
                    ))}
                  </MenuGroup>
                </>
              )}
            </>
          </MenuList>
        </Portal>
      </Menu>

      <ProfileSettingsDialog
        person={{ ...currentUser }}
        isOpen={shouldShowProfileSettings}
        onClose={() => setShouldShowProfileSettings(false)}
        onUpdatePerson={(person) => {
          handleUpdateProfile(person);
          setShouldShowProfileSettings(false)
        }}
      />
    </>
  );
};

PresenceDetails.defaultProps = {
  defaultOpen: false,
};
