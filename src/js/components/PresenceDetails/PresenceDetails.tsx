import { withErrorBoundary } from 'react-error-boundary';
import React from "react";

import { Text, Tooltip, Avatar, AvatarGroup, Menu, MenuDivider, MenuButton, MenuList, MenuGroup, MenuItem, Button, Portal } from '@chakra-ui/react';

import { ProfileSettingsDialog } from "@/ProfileSettingsDialog";

export interface PresenceDetailsProps {
  hostAddress: HostAddress;
  currentUser?: Person;
  currentPageMembers: Person[];
  differentPageMembers: Person[];
  handleUpdateProfile(person: Person): void;
  handleCopyHostAddress(hostAddress: HostAddress): void;
  handleCopyPermalink?(): void;
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
    <Tooltip label={`${showablePersons.length} member${showablePersons.length === 1 ? '' : 's'} online`}>
      <Button
        ref={ref as any}
        as={MenuButton}
        size="sm"
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


export const PresenceDetails = withErrorBoundary((props: PresenceDetailsProps) => {
  const {
    hostAddress,
    currentUser,
    currentPageMembers,
    differentPageMembers,
    handleCopyHostAddress,
    handleCopyPermalink,
    handlePressMember,
    handleUpdateProfile,
    connectionStatus,
  } = props;
  const showablePersons = [...currentPageMembers, ...differentPageMembers];
  const [shouldShowProfileSettings, setShouldShowProfileSettings] = React.useState(false);

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
                <>
                  <MenuItem onClick={() => handleCopyHostAddress(hostAddress)}>
                    Copy link to database
                  </MenuItem>
                  {handleCopyPermalink && <MenuItem onClick={() => handleCopyPermalink()}>
                    Copy link to page
                  </MenuItem>}
                </>
              )}

              {currentUser && (
                <>
                  <MenuDivider />
                  <MenuItem onClick={() => setShouldShowProfileSettings(true)} icon={<Avatar
                    size="xs"
                    marginBlock={-1}
                    name={currentUser.username}
                    bg={currentUser.color}
                  />}>Edit appearance</MenuItem>
                </>
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
}, { fallback: <></> });
