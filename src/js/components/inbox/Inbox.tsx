import {
  Box,
  Divider,
  Center,
  MenuList,
  Menu,
  MenuButton,
  ButtonGroup,
  Button,
  VStack,
  Text,
  Avatar,
  MenuOptionGroup,
  MenuItemOption
} from "@chakra-ui/react";
import {
  InboxView,
  InboxViewListItem,
  InboxViewListHeader,
  InboxViewListGroupHeader,
  InboxViewListBody,
  InboxViewContent,
  InboxViewList
} from '../Layout/InboxView';
import * as React from "react";
import { faker } from "@faker-js/faker";
import { motion } from "framer-motion";
import { useNotifications } from "../utils/useNotifications";

type PAGE = {
  name: string
  url: string
  breadcrumb: any
}

type BLOCK = {
  string: string
  url: string
  breadcrumb: any
}

type PROPERTY = {
  name: string
  breadcrumb: any
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
    return <Text as="span" color="foreground.secondary"><Text color="foreground.primary" as="span" fontWeight="medium">{subjectName}</Text> created <Text as="span" color="foreground.primary" fontWeight="medium">{objectName}</Text></Text>;
  } else if (type === "Edited") {
    return <Text as="span" color="foreground.secondary"><Text color="foreground.primary" as="span" fontWeight="medium">{subjectName}</Text> edited <Text as="span" color="foreground.primary" fontWeight="medium">{objectName}</Text></Text>;
  } else if (type === "Deleted") {
    return <Text as="span" color="foreground.secondary"><Text color="foreground.primary" as="span" fontWeight="medium">{subjectName}</Text> deleted <Text as="span" color="foreground.primary" fontWeight="medium">{objectName}</Text></Text>;
  } else if (type === "Comments") {
    return <Text as="span" color="foreground.secondary"><Text color="foreground.primary" as="span" fontWeight="medium">{subjectName}</Text> commented on <Text as="span" color="foreground.primary" fontWeight="medium">{objectName}</Text></Text>;
  } else if (type === "Mentions") {
    return <Text as="span" color="foreground.secondary"><Text color="foreground.primary" as="span" fontWeight="medium">{subjectName}</Text> mentioned you in  <Text as="span" color="foreground.primary" fontWeight="medium">{objectName}</Text></Text>;
  } else if (type === "Assignments") {
    return <Text as="span" color="foreground.secondary"><Text color="foreground.primary" as="span" fontWeight="medium">{subjectName}</Text> assigned you to <Text as="span" color="foreground.primary" fontWeight="medium">{objectName}</Text></Text>;
  } else if (type === "Completed") {
    return <Text as="span" color="foreground.secondary"><Text color="foreground.primary" as="span" fontWeight="medium">{subjectName}</Text> completed <Text as="span" color="foreground.primary" fontWeight="medium">{objectName}</Text></Text>;
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
  breadcrumb: faker.lorem.words(),
})
const makeNotificationBlockObject = () => ({
  string: faker.lorem.sentence(),
  url: faker.internet.url(),
  breadcrumb: faker.lorem.words(),
})
const makeNotificationPropertyObject = () => ({
  name: faker.lorem.sentence(),
  breadcrumb: faker.lorem.words(),
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
export const availableFilters = [
  READ_FILTER, ARCHIVED_FILTER
]
export const DEFAULT_FILTERS = [READ_FILTER.id]

export const Inbox = () => {
  const [items, setItems] = React.useState(ITEMS);

  const {
    selectedItemId,
    selectedItemRef,
    selectedItem,
    hasMeaningfulFilters,
    filterIds,
    setFilterIds,
    filteredItems,
    groupedFilteredItems,
    getActionsForNotification,
    resetFilters,
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

  const actions = <ButtonGroup isAttached size="xs">
    <Menu closeOnSelect={false}>
      <Button as={MenuButton}>Filter</Button>
      <MenuList>
        <MenuOptionGroup
          type="checkbox"
          value={filterIds}
          onChange={(value) => setFilterIds(value)}
        >
          {availableFilters.map((filter) => {
            return (<MenuItemOption
              key={filter.id}
              value={filter.id}
            >
              {filter.label}
            </MenuItemOption>)
          })}
        </MenuOptionGroup>
      </MenuList>
    </Menu>
  </ButtonGroup>;

  const itemsAsList = filteredItems.map((i) => (
    <InboxViewListItem
      message={messageForNotification(i)}
      isSelected={i.id === selectedItemId}
      actions={getActionsForNotification(i)}
      onOpen={openItem}
      onSelect={selectItem}
      onDeselect={deselectItem}
      onMarkAsRead={markAsRead}
      onMarkAsUnread={markAsUnread}
      onMarkAsArchived={markAsArchived}
      onMarkAsUnarchived={markAsUnarchived}
      key={i.id}
      {...i}
    />));

  const flattenedGroupedItems: any[] =
    Object.keys(groupedFilteredItems)
      .flatMap(key => ([{ type: "listHeading", heading: key, count: groupedFilteredItems[key].length }, ...groupedFilteredItems[key]]));

  const groupedItems = flattenedGroupedItems.map((i) => {
    if (i.type === 'listHeading') {
      const { heading, count } = i;
      return <InboxViewListGroupHeader key={heading.toString()} title={heading.toString()} count={count} />
    }
    else {
      return (
        <InboxViewListItem
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
        />)
    }
  });

  return (
    <>
      <InboxView>
        <InboxViewList>
          <InboxViewListHeader
            title="Inbox"
            subtitle={`${filteredItems.length} items`}
            actions={actions}
          />
          <InboxViewListBody>
            {!!filteredItems.length ?
              groupedItems
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
                <Box py={4}>
                  {hasMeaningfulFilters ? <MessageNoNotificationsHere onClearFilters={resetFilters} /> : <MessageAllDone />}
                </Box>
              </Center>}
          </InboxViewListBody>
        </InboxViewList>
        <InboxViewContent>
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
        </InboxViewContent>
      </InboxView>
    </>
  );
}
