import { ChevronDownIcon } from '@/Icons/Icons';
import { ActionsListItem, mapActionsToMenuList } from '@/utils/mapActionsToMenuList';
import { Heading, VStack, Menu, MenuButton, MenuList, Portal, Button } from '@chakra-ui/react';

interface LocationIndicatorProps {
  currentLocationName: string;
  isVisible: boolean;
  breadcrumbs?: {}[],
  actions?: ActionsListItem[],
  uid?: string
}

export const LocationIndicator = (props: LocationIndicatorProps) => {
  const { isVisible,
    currentLocationName,
    breadcrumbs,
    actions,
    uid
  } = props;

  return <VStack
    opacity={isVisible ? 1 : 0}
    transform={isVisible ? 'translateY(0)' : 'translateY(25%)'}
    transition="transform 0.2s ease-in-out, opacity 0.2s ease-in-out"
    spacing={-0.25} align="flex-start" flex={1}
    maxWidth="50%"
    tabIndex={isVisible ? 0 : -1}
    pointerEvents={isVisible ? 'all' : 'none'}
  >
    {(actions) ? (
      <Menu placement="bottom" isLazy>
        <MenuButton
          as={Button}
          variant="ghost"
          fontSize="sm"
          size="sm"
          overflow="hidden"
          textAlign="start"
          sx={{
            "> span": {
              whiteSpace: "nowrap",
              overflow: "hidden",
              textOverflow: "ellipsis",
              alignItems: "flex-start",
              justifyContent: "flex-start"
            },
            ...(breadcrumbs && {
              ".chakra-button__icon": {
                alignSelf: "flex-end"
              }
            })
          }}
          {...(breadcrumbs && {
            height: "auto",
            py: 1,
            px: 2
          })}
          rightIcon={actions ? <ChevronDownIcon color="foreground.secondary" /> : undefined}>
          {breadcrumbs && <Heading noOfLines={0} maxWidth="100%" display="block" textTransform="uppercase" textAlign="start" color="foreground.secondary" fontSize="50%">{breadcrumbs[0].label}</Heading>}
          {currentLocationName}
        </MenuButton>
        <Portal>
          <MenuList>
            {mapActionsToMenuList({ target: uid, menuItems: actions })}
          </MenuList>
        </Portal>
      </Menu>
    ) : (
      <Heading pl={2} whiteSpace="nowrap" noOfLines={0} color="foreground.secondary" size="xs">{currentLocationName}</Heading>
    )}
  </VStack>
}