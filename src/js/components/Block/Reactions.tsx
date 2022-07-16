import {
  Button, ButtonGroup, Text, Box, Tooltip,
} from "@chakra-ui/react";
import { EmojiPickerPopover } from '@/EmojiPicker/EmojiPicker';

type ReactionId = string;
type UserId = string;
type Reaction = [ReactionId, UserId[]];

export interface ReactionsProps {
  reactions: Reaction[];
  currentUser: UserId;
  onToggleReaction: (reactionItem: ReactionId, user: UserId) => void;
}

interface ReactionItemProps {
  reaction: Reaction,
  currentUser: UserId
  onToggleReaction: (reactionItem: ReactionId, userId: UserId) => void,
}

const ReactionItem = ({ reaction, onToggleReaction, currentUser }: ReactionItemProps) => {
  const reactionItem: ReactionId = reaction[0];
  const users: UserId[] = reaction[1];
  const usersCount: number = reaction[1].length;
  const isFromCurrentUser: boolean = users.includes(currentUser);

  return <Button
    key={reactionItem}
    as={isFromCurrentUser ? "button" : "div"}
    display="flex"
    gap={1}
    position="relative"
    isActive={isFromCurrentUser}
    onClick={isFromCurrentUser ? () => onToggleReaction(reactionItem, currentUser) : undefined}
    {...isFromCurrentUser && {
      sx: {
        "&[data-active]:hover": {
          bg: "interaction.surface.hover",
        }
      }
    }}
  >
    <Tooltip label={users.join(', ')}>
      <Box position="absolute" inset={0} />
    </Tooltip>
    <Text transform="scale(1.35)" fontSize="md">{reactionItem}</Text>
    <Text fontSize="xs" color="foreground.secondary">{usersCount < 10 ? usersCount : "9+"}</Text>
  </Button>
}

export const Reactions = ({ reactions, onToggleReaction, currentUser }: ReactionsProps): JSX.Element | null => {
  if (!reactions.length) return null;

  return (
    <ButtonGroup
      className="block-reactions"
      gridArea="reactions"
      size="xs"
      mb={1}
      isAttached={true}
      borderRadius="md"
    >
      {reactions.map(reaction => (<ReactionItem
        key={reaction[0]}
        reaction={reaction}
        onToggleReaction={onToggleReaction}
        currentUser={currentUser}
      />))}
      <EmojiPickerPopover onEmojiSelected={(event) => onToggleReaction(event.detail.unicode, currentUser)} />
    </ButtonGroup>
  );
};
