import React from 'react';
import { VStack, Grid, HStack, Box, Text, Heading, Button, Checkbox } from '@chakra-ui/react';
import { Reorder } from 'framer-motion';

/*
How will the notification look?
    Horizontal stack
    - Icon to indicate state of read/unread
    - checkbox
    - A card for task title
        - underneath the title show the message
    - Mark as read
    - Mark as done
*/

export const KanbanCard = (props) => {
  const { card, isSelected } = props;

  return <Box
    as={Reorder.Item}
    key={card.toString()}
    value={card}
    borderRadius="sm"
    h="4rem"
    listStyleType={"none"}
    border="1px solid transparent"
    bg="background.floor"
    _hover={{
      bg: 'background.upper',
      border: "1px solid",
      borderColor: "background.floor"
    }}
  >
    <HStack h="100%">
      <Box bg="green" w="3px" h="100%">
      </Box>
      <Checkbox></Checkbox>
      <Box _groupHover={{display:"None"}} p="8px" shadow='md' h="90%" w="100%">
              <Heading fontSize='md'> Title {card}</Heading>
              <Text > message {card} </Text>
      </Box>
      <Button  _groupHover={{display:"block"}}> Mark as read </Button>
    </HStack>
  </Box>;
}

export const KanbanColumn = (props) => {
  const { name, cards } = props;
  const [items, setItems] = React.useState(cards);

  return (
    <Box w="1000px"
        p={2}
        borderRadius="md"
        bg="background.upper">
      <Heading color="foreground.secondary" size="xl">{name}</Heading>
      <HStack py={2}>
        <Checkbox>Show Hidden messages</Checkbox>
        <Checkbox> Hide read messages </Checkbox>
      </HStack>
      <VStack
        w="100%"
        as={Reorder.Group}
        align="stretch"
        listStyleType={"none"}
        spacing={2}
        axis="y"
        values={items}
        onReorder={setItems}
        m="5px"
      >
        {items.map((card) => (
          <KanbanCard key={card.toString()} card={card} isSelected={false} />
        ))}
      </VStack>
    </Box>
  );
}

export const KanbanSwimlane = (props) => {
  const { name, children } = props;
  return (
    <VStack align="stretch" borderRadius="md" py={2}>
      <Heading color="foreground.secondary" size="md">{name}</Heading>
      <Grid gap={2} p={0} templateRows="1" templateColumns="repeat(auto-fill, 15rem)" borderRadius="sm">
        {children}
      </Grid>
    </VStack>
  );
}

export const KanbanBoard = (props) => {
  const { name, children } = props;
  return (
    <VStack align="stretch" spacing={3} py={2}>
      <Heading size="md">{name}</Heading>
      {children}
    </VStack>
  );
}

export const ExampleKanban = () => {
  const columns = ["Backlog", "In Progress", "Done"];
  const swimlanes = ["High", "Low"];
  const cards = ["Card 1", "Card 2", "Card 3"];

  return <KanbanBoard name="Task Board">
    {swimlanes.map((swimlane) =>
      <KanbanSwimlane name={swimlane}>
        {columns.map((column) =>
          <KanbanColumn name={column}>
            {cards.map((card) =>
              <KanbanCard>
                {card}
              </KanbanCard>
            )}
          </KanbanColumn>
        )}
      </KanbanSwimlane>
    )}
  </KanbanBoard>
}