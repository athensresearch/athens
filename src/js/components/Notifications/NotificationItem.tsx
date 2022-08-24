import { ArchiveIcon } from "@/Icons/Icons";
import { mapActionsToButtons } from "@/utils/mapActionsToButtons";
import { Box, ButtonGroup, HStack, Text, VStack } from "@chakra-ui/react";
import { motion } from "framer-motion";

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

const NotificationStatusIndicator = ({ isRead, }) => {
  return <Box
    flexShrink={0}
    borderRadius="100%"
    position="relative"
    top="-0.15ch"
    w="0.5em"
    h="0.5em"
    {...isRead ? {
      bg: "transparent",
      boxShadow: 'inset 0 0 0 1px foreground.secondary'
    } : {
      bg: "info",
    }}
  />
}

export const NotificationItem = (props) => {
  const { notification, ...otherProps } = props;
  const { id, isRead, type, isArchived, body, object, notificationTime } = notification;
  const { onOpenItem, onMarkAsRead, onMarkAsUnread, onArchive, onUnarchive, ...boxProps } = otherProps;
  console.log(props);

  const getActionsForNotification = (notification) => {
    const actions = [];
    if (notification.isArchived) {
      actions.push({
        label: "Unarchive",
        fn: () => onUnarchive(notification.id),
        icon: <ArchiveIcon />
      });
    } else {
      actions.push({
        label: "Archive",
        fn: (e) => onArchive(e, notification.id),
        icon: <ArchiveIcon />
      });
    }
    return actions;
  }

  return <VStack
    layout
    key={id}
    as={motion.div}
    initial={{
      opacity: 0,
      height: 0,
    }}
    animate={{
      height: "auto",
      opacity: 1,
    }}
    exit={{
      height: 0,
      opacity: 0,
    }}
    p={2}
    spacing={1}
    flexShrink={0}
    overflow="hidden"
    align="stretch"
    userSelect="none"
    _hover={{
      cursor: "pointer"
    }}
    borderRadius="md"
    bg={"interaction.surface"}
    color={isRead ? "foreground.secondary" : "foreground.primary"}
    {...boxProps}
  >
    <HStack
      align="baseline"
      textAlign="left"
      spacing={1.5}
    >
      <NotificationStatusIndicator isRead={isRead} />
      <VStack flexShrink={1} spacing={0} align="stretch">
        <Text fontSize="sm">{messageForNotification(notification)}</Text>
        {body && <Text fontSize="sm">{body}</Text>}
      </VStack>
    </HStack>
    <HStack justifyContent="space-between">
      <Text fontSize="sm"
        marginLeft="14px"
        color="gray">{notificationTime}</Text>
      <ButtonGroup
        flex="0 0 auto"
        onDoubleClick={(e) => e.stopPropagation()}
        size="xs"
        alignSelf="flex-end"
      >
        {mapActionsToButtons(getActionsForNotification(notification), 1)}
      </ButtonGroup>
    </HStack>
  </VStack>
}

