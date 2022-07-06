import {
  Tooltip,
  Box,
  Divider,
  Center,
  Flex,
  MenuList,
  Menu,
  MenuButton,
  ButtonGroup,
  Button,
  VStack,
  Text,
  Avatar,
  MenuOptionGroup,
  MenuItemOption,
  PopoverContent,
  Popover,
  IconButton,
  PopoverTrigger,
  PopoverBody,
  Portal,
  PopoverHeader,
  PopoverCloseButton
} from "@chakra-ui/react";
import {
  InboxView,
  InboxViewListHeader,
  InboxViewListGroupHeader,
  InboxViewListBody,
  InboxViewContent,
  InboxViewList
} from '../Layout/InboxView';
import { InboxViewListItem } from "../Layout/InboxViewListItem";
import * as React from "react";
import { faker } from "@faker-js/faker";
import { motion } from "framer-motion";
import { useNotifications } from "../utils/useNotifications";
import { CheckmarkIcon } from "@/Icons/Icons";

type PAGE = {
  name: string
  url: string
  breadcrumb: string[]
}

type BLOCK = {
  string: string
  url: string
  breadcrumb: string[]
}

type PROPERTY = {
  name: string
  breadcrumb: string[]
}

type OBJECT = PAGE | PROPERTY | BLOCK;

const notificationTypes = ["Created", "Edited", "Deleted", "Comments", "Mentions", "Assignments", "Completed"]
type NOTIFICATION_TYPE = typeof notificationTypes[number];

type NOTIFICATION = {
  id: string
  // time: string
  type: NOTIFICATION_TYPE
  subject: Person
  object: OBJECT
  isRead: boolean,
  isArchived: boolean
}

const messageForNotification = (notification: NOTIFICATION): React.ReactNode => {
  const { type, subject, object } = notification;
  const subjectName = subject.username;
  const objectName = object.string || object.name;

  if (type === "Created") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> created <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Edited") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> edited <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Deleted") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> deleted <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Comments") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> commented on <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Mentions") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> mentioned you in  <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Assignments") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> assigned you to <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Completed") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> completed <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  }
}

const makeNotificationSubject = () => ({
  personId: faker.datatype.uuid(),
  username: faker.name.findName(),
  color: faker.internet.color(),
})

const makeNotificationPageObject = () => ({
  name: faker.lorem.words(),
  url: faker.internet.url(),
  breadcrumb: new Array(4).fill(true).map(() => (faker.lorem.words())),
})
const makeNotificationBlockObject = () => ({
  string: faker.lorem.sentence(),
  url: faker.internet.url(),
  breadcrumb: new Array(4).fill(true).map(() => (faker.lorem.words())),
})
const makeNotificationPropertyObject = () => ({
  name: faker.lorem.sentence(),
  breadcrumb: new Array(4).fill(true).map(() => (faker.lorem.words())),
})
const makeNotificationObject = () => faker.helpers.arrayElement([makeNotificationPageObject,
  makeNotificationBlockObject,
  makeNotificationPropertyObject])()

const makeNotification = (): NOTIFICATION => ({
  id: faker.datatype.uuid(),
  // time: faker.date.past().toISOString(),
  type: faker.helpers.arrayElement(notificationTypes),
  subject: makeNotificationSubject(),
  object: makeNotificationObject(),
  isRead: false,
  isArchived: false
})

const MessageAllDone = () => <Text fontSize="sm">All done!</Text>
const MessageNoNotificationsHere = ({ onClearFilters }) => <VStack>
  <Text color="foreground.secondary" fontSize="sm">No notifications here</Text>
  <Button size="sm" onClick={onClearFilters}>Clear filters</Button>
</VStack>

const ITEMS = new Array(12).fill(true).map(() => makeNotification());

export const READ_FILTER = { id: "read", label: "Read", fn: (n) => !n.isRead }
const ARCHIVED_FILTER = { id: "archived", label: "Archived", fn: (n) => n.isArchived }
export const DEFAULT_FILTERS = [READ_FILTER.id]

const availableGroupings = ["type", "isRead", "isArchived"]
export const availableFilters = [
  READ_FILTER, ARCHIVED_FILTER
]

export const InboxItemsList = () => {
  const [items, setItems] = React.useState(ITEMS);

  const {
    // hasMeaningfulFilters,
    // filterIds,
    // setFilterIds,
    // filteredItems,
    // setGrouping,
    // grouping,
    // groupedFilteredSortedItems,
    // resetFilters,
    selectedItemId,
    selectedItemRef,
    selectedItem,
    getActionsForNotification,
    selectItem,
    deselectItem,
    markAsRead,
    markAsArchived,
    markAsUnread,
    markAsUnarchived,
    openItem,
  } = useNotifications(items, setItems, availableFilters, DEFAULT_FILTERS);

  const itemsList = items.map((i) => <InboxViewListItem
    message={messageForNotification(i)}
    actions={getActionsForNotification(i)}
    isSelected={i.id === selectedItemId}
    onOpen={openItem}
    onSelect={selectItem}
    onDeselect={deselectItem}
    onMarkAsRead={markAsRead}
    onMarkAsUnread={markAsUnread}
    onMarkAsArchived={markAsArchived}
    onMarkAsUnarchived={markAsUnarchived}
    key={i.id}
    {...i}
  />);

  return <InboxViewListBody>
    {!!itemsList.length ?
      itemsList
      : <Center
        as={motion.div}
        key="empty"
        animate={{
          height: "auto",
          opacity: 1,
        }}
        exit={{
          height: 0,
          opacity: 0,
        }}
      >
        {/* <Box py={4}>
        {hasMeaningfulFilters ? <MessageNoNotificationsHere onClearFilters={resetFilters} /> : <MessageAllDone />}
      </Box> */}
      </Center>}
  </InboxViewListBody>
}

export const Inbox = ({ showContent = true }) => {
  const [items, setItems] = React.useState(ITEMS);

  const {
    // hasMeaningfulFilters,
    // filterIds,
    // setFilterIds,
    // filteredItems,
    // setGrouping,
    // grouping,
    // groupedFilteredSortedItems,
    // resetFilters,
    selectedItemId,
    selectedItemRef,
    selectedItem,
    getActionsForNotification,
    selectItem,
    deselectItem,
    markAsRead,
    markAsArchived,
    markAsUnread,
    markAsUnarchived,
    openItem,
  } = useNotifications(items, setItems, availableFilters, DEFAULT_FILTERS);

  React.useLayoutEffect(() => {
    if (selectedItemId && selectedItemRef.current) {
      selectedItemRef.current.scrollIntoView({ behavior: "smooth", block: "nearest" });
    }
  }, [selectedItemId])

  // const actions = <ButtonGroup isAttached size="xs">
  //   <Menu>
  //     <Button as={MenuButton}>Group <Text display="inline" color="foreground.secondary" textTransform="capitalize">{grouping}</Text></Button>
  //     <MenuList>
  //       <MenuOptionGroup
  //         type="radio"
  //         value={grouping}
  //         onChange={(value) => setGrouping(value as string)}
  //       >
  //         {availableGroupings.map((grouping) => {
  //           return (<MenuItemOption
  //             textTransform="capitalize"
  //             key={grouping}
  //             value={grouping}
  //           >
  //             {grouping}
  //           </MenuItemOption>)
  //         })}
  //       </MenuOptionGroup>
  //     </MenuList>
  //   </Menu>
  //   <Menu closeOnSelect={false}>
  //     <Button as={MenuButton}>Filter</Button>
  //     <MenuList>
  //       <MenuOptionGroup
  //         type="checkbox"
  //         value={filterIds}
  //         onChange={(value) => setFilterIds(value)}
  //       >
  //         {availableFilters.map((filter) => {
  //           return (<MenuItemOption
  //             key={filter.id}
  //             value={filter.id}
  //           >
  //             {filter.label}
  //           </MenuItemOption>)
  //         })}
  //       </MenuOptionGroup>
  //     </MenuList>
  //   </Menu>
  // </ButtonGroup>;


  // const itemsAsList = filteredItems.map((i) => (
  //   <InboxViewListItem
  //     message={messageForNotification(i)}
  //     isSelected={i.id === selectedItemId}
  //     actions={getActionsForNotification(i)}
  //     onOpen={openItem}
  //     onSelect={selectItem}
  //     onDeselect={deselectItem}
  //     onMarkAsRead={markAsRead}
  //     onMarkAsUnread={markAsUnread}
  //     onMarkAsArchived={markAsArchived}
  //     onMarkAsUnarchived={markAsUnarchived}
  //     key={i.id}
  //     {...i}
  //   />));


  // const groupedItemsList = Object.keys(groupedFilteredSortedItems)
  //   .flatMap(key => ([{ type: "listHeading", heading: key, count: groupedFilteredSortedItems[key].length }, ...groupedFilteredSortedItems[key]])).map((i) => {
  //     if (i.type === 'listHeading') {
  //       const { heading, count } = i;
  //       return <InboxViewListGroupHeader key={heading.toString()} title={heading.toString()} count={count} />
  //     }
  //     else {
  //       return (
  //         <InboxViewListItem
  //           message={messageForNotification(i)}
  //           actions={getActionsForNotification(i)}
  //           isSelected={i.id === selectedItemId}
  //           onOpen={openItem}
  //           onSelect={selectItem}
  //           onDeselect={deselectItem}
  //           onMarkAsRead={markAsRead}
  //           onMarkAsUnread={markAsUnread}
  //           onMarkAsArchived={markAsArchived}
  //           onMarkAsUnarchived={markAsUnarchived}
  //           key={i.id}
  //           {...i}
  //         />)
  //     }
  //   });

  const itemsList = items.map((i) => <InboxViewListItem
    message={messageForNotification(i)}
    actions={getActionsForNotification(i)}
    isSelected={i.id === selectedItemId}
    onOpen={openItem}
    onSelect={selectItem}
    onDeselect={deselectItem}
    onMarkAsRead={markAsRead}
    onMarkAsUnread={markAsUnread}
    onMarkAsArchived={markAsArchived}
    onMarkAsUnarchived={markAsUnarchived}
    key={i.id}
    {...i}
  />);

  const hasMeaningfulFilters = false;

  return (
    <>
      <InboxView>
        <InboxViewList>
          <InboxViewListHeader
            title="Inbox"
            subtitle={`${items.length} items`}
          // actions={actions}
          />
          <InboxViewListBody>
            {!!itemsList.length ?
              itemsList
              : <Center
                as={motion.div}
                key="empty"
                animate={{
                  height: "auto",
                  opacity: 1,
                }}
                exit={{
                  height: 0,
                  opacity: 0,
                }}
              >
                {/* <Box py={4}>
                  {hasMeaningfulFilters ? <MessageNoNotificationsHere onClearFilters={resetFilters} /> : <MessageAllDone />}
                </Box> */}
              </Center>}
          </InboxViewListBody>
        </InboxViewList>
        {showContent && <InboxViewContent>
          <Center flex={1} maxHeight="100%">
            {selectedItem
              ? <Text flex={1} textAlign="center">
                <Box>
                  <Avatar
                    name={selectedItem.subject.username}
                    color={selectedItem.subject.color}
                  />
                  <Text>{selectedItem.subject.username}</Text>
                </Box>
                {messageForNotification(selectedItem)}
                <Box>
                  <Text>{selectedItem?.object?.name || selectItem?.object?.string}</Text>
                  <Divider />
                  <Text>{selectedItem?.object?.breadcrumb}</Text>
                </Box>
              </Text>
              : <Text flex={1} textAlign="center" color="foreground.secondary">No item selected</Text>}
          </Center>
        </InboxViewContent>}
      </InboxView>
    </>
  );
}

export const NotificationsPopover = () => {
  return <Popover closeOnBlur={false}>
    <Tooltip shouldWrapChildren label="Notifications">
      <PopoverTrigger>
        <IconButton aria-label="Notifications" icon={<CheckmarkIcon />} />
      </PopoverTrigger>
    </Tooltip>
    <PopoverContent maxWidth="max-content" maxHeight="calc(100vh - 4rem)">
      <PopoverCloseButton />
      <PopoverHeader>Notifications</PopoverHeader>
      <Flex p={0} as={PopoverBody} flexDirection="column" overflow="hidden">
        <InboxItemsList />
      </Flex>
    </PopoverContent>
  </Popover>
}