import { ChevronDownIcon } from '@/Icons/Icons';
import { mapActionsToMenuList, ActionsListItem } from '@/utils/mapActionsToMenuList';
import { Text, Breadcrumb, BreadcrumbItem, BreadcrumbLink, Heading, HStack, IconButton, Menu, MenuButton, MenuDivider, MenuItem, MenuList, VStack, Portal } from '@chakra-ui/react';

const Title = (props) => {
  const { children, ...rest } = props;
  return <Heading
    flex="0 1 auto"
    border="1px solid red"
    isTruncated={true}
    fontSize="sm"
    {...rest}
  >
    {children}
  </Heading>
}

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

  return (<VStack
    opacity={isVisible ? 1 : 0}
    transform={isVisible ? 'translateY(0)' : 'translateY(1rem)'}
    transition="transform 0.2s ease-in-out, opacity 0.2s ease-in-out"
    spacing={-1}
    align="flex-start"
    border="1px solid green"
    overflow="hidden"
    justify="center"
    flex="0 1 auto"
  >
    {/* {path && <Breadcrumb fontSize="xs">
      {path.map(({ label, path }) => <BreadcrumbItem key={path}><BreadcrumbLink onClick={() => console.log(path)} color="foreground.secondary">{label}</BreadcrumbLink></BreadcrumbItem>)}
    </Breadcrumb>} */}
    {actions ?
      <Menu placement="bottom" isLazy>
        <MenuButton sx={{ "& > span": { display: "contents" } }} overflow="hidden">
          <HStack overflow="hidden">
            <Title>
              {title} {title} {title} {title} {title} {title} {title} {title}
            </Title>
            <ChevronDownIcon />
          </HStack>
        </MenuButton>
        <Portal>
          <MenuList>
            {mapActionsToMenuList({ target: uid, menuItems: actions })}
          </MenuList>
        </Portal>
      </Menu>
      : <Title>{title}</Title>}
  </VStack>);
}