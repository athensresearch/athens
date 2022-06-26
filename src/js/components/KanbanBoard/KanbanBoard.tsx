import React from 'react';
import { VStack, Grid, HStack, Box, Text, Heading } from '@chakra-ui/react';

export const KanbanCard = (props) => {
  const { children } = props;
  return (
    <Box draggable borderRadius="sm" minHeight="4rem" p={2} bg="background.floor">
      <Text>{children}</Text>
    </Box>
  );
}

export const KanbanColumn = (props) => {
  const { name, id, children } = props;
  return (
    <VStack draggable spacing={2} p={2} align="stretch" borderRadius="md" bg="background.upper">
      <Heading color="foreground.secondary" size="sm">{name}</Heading>
      {children}
    </VStack>
  );
}

export const KanbanSwimlane = (props) => {
  const { name, children } = props;
  return (
    <VStack draggable align="stretch" borderRadius="md" py={2}>
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
