import { ChevronDownIcon } from '@/Icons/Icons';
import { mapActionsToMenuList, ActionsListItem } from '@/utils/mapActionsToMenuList';
import { Heading, VStack, Menu, MenuButton, MenuList, Portal, Button, Breadcrumb, BreadcrumbLink, BreadcrumbItem } from '@chakra-ui/react';

interface LocationIndicatorProps {
  title: string;
  uid: string;
  isVisible: boolean;
  type: "node" | "block",
  path?: { label: string, path: string }[]
  actions?: ActionsListItem[];
}

export const LocationIndicator = (props: LocationIndicatorProps) => {
  const { isVisible, ...locationProps } = props;
  const { title, uid, type, path, actions } = locationProps;
  if (type !== 'node') return null;

  return <VStack
    opacity={isVisible ? 1 : 0}
    transform={isVisible ? 'translateY(0)' : 'translateY(1rem)'}
    transition="transform 0.2s ease-in-out, opacity 0.2s ease-in-out"
    spacing={-0.25} align="flex-start" flex={1}
    maxWidth="50%"
    tabIndex={isVisible ? 0 : -1}
    pointerEvents={isVisible ? 'all' : 'none'}
  >
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
          ...(path && {
            ".chakra-button__icon": {
              alignSelf: "flex-end"
            }
          })
        }}
        {...(path && {
          height: "auto",
          py: 1,
          px: 2
        })}
        rightIcon={<ChevronDownIcon color="foreground.secondary" />}>
        {path && <Heading isTruncated noOfLines={0} maxWidth="100%" display="block" textTransform="uppercase" textAlign="start" color="foreground.secondary" fontSize="50%">{path[0].label}</Heading>}
        {title}
      </MenuButton>
      <Portal>
        <MenuList>
          {mapActionsToMenuList({ target: uid, menuItems: actions })}
        </MenuList>
      </Portal>
    </Menu>
  </VStack >
}
