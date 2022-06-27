import React from 'react';
import { VStack, Grid, HStack, Box, Text, Heading } from '@chakra-ui/react';
import { Reorder } from 'framer-motion';

export const KanbanCard = (props) => {
  const { card, isSelected } = props;

  return <Box
    // as={Reorder.Item}
    key={card.toString()}
    value={card}
    borderRadius="sm"
    minHeight="4rem"
    listStyleType={"none"}
    border="1px solid transparent"
    p={2}
    bg="background.floor"
    _hover={{
      bg: 'background.upper',
      border: "1px solid",
      borderColor: "background.floor"
    }}
  >
    <Text>{card}</Text>
  </Box>;
}

export const KanbanColumn = (props) => {
  const { name, children } = props;
  console.log("column", props)
  // const [items, setItems] = React.useState(["Card 1", "Card 2", "Card 3"]);

  return (
    <Box>
      <VStack
        // as={Reorder.Group}
        align="stretch"
        listStyleType={"none"}
        spacing={2}
        p={2}
        borderRadius="md"
        bg="background.upper"
        axis="y"
        // values={items}
        // onReorder={setItems}
      >
        <Heading color="foreground.secondary" size="sm">{name}</Heading>
      {children}
      </VStack>
    </Box>
  );
}

        // {<!--items.map((card) => (
        //   <KanbanCard key={card.toString()} card={card} isSelected={false} />
        // ))-->}

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
            {}
          </KanbanColumn>
        )}
      </KanbanSwimlane>
    )}
  </KanbanBoard>
}

export const ExampleKanban2 = (props) => {
  const { boardData, columns } = props
  console.log(boardData, columns)
  // return (
  //   <h1>hi</h1>
  // )
  return <KanbanBoard name="Task Board">
    {Object.entries(boardData).map(([x, y]) =>
      <KanbanSwimlane name={x}>
        {columns.map(x =>
          <KanbanColumn name={x}>
          {y[x] &&  y[x].map(({id, title, status, assignee, project}) =>
            <KanbanCard key={id} card={title} />
            )}
          </KanbanColumn>
          )}

      </KanbanSwimlane>
    )}
  </KanbanBoard>
}
