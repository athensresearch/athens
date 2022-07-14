import React from 'react';
import { VStack, Grid, HStack, Box, Text, Heading, Button } from '@chakra-ui/react';
import { Reorder } from 'framer-motion';

export const KanbanCard = (props) => {
  const { id, isSelected, columns, onUpdateStatusClick } = props;
  const columnIndex = columns.indexOf(status);
  const columnCount = columns.length;

  const title = props[":task/title"]
  const assignee = props[":task/assignee"]
  const dueDate = props[":task/due-date"]
  const status = props[":task/status"]
  const priority = props[":task/priority"]
  const project = props[":task/project"]

  return <Box
    // as={Reorder.Item}
    key={title.toString()}
    value={title}
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

    <Text>{title}</Text>
  {(columnIndex > 0) &&
    <Button value="left" onClick={(e) => {
      onUpdateStatusClick(id, columns[columnIndex-1])
    }}>←</Button>}
  {(columnIndex < (columnCount - 1)) &&
    <Button value="right" onClick={(e) =>
      onUpdateStatusClick(id, columns[columnIndex+1])
    }>→</Button>}
  </Box>;
}

export const KanbanColumn = (props) => {
  const { name, children } = props;
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

export const AddCardButton = (props) => {
  const { children, column, project, onAddNewCardClick } = props
  return <Button size={"sm"} variant={"ghost"} fontWeight={"light"} onClick={() =>
    onAddNewCardClick(project, column)
    }>
    + New
  </Button>
};

export const AddColumnButton = (props) => {
  const { children, } = props
  return <Button size={"sm"} variant={"ghost"} fontWeight={"light"} onClick={() => console.log("TODO: new column")}>
    + New Column
  </Button>
};

export const AddSwimlaneButton = (props) => {
  const { children, } = props
  return <Button size={"sm"} variant={"ghost"} fontWeight={"light"} onClick={() => console.log("TODO: new swimlane")}>
    + New Swimlane
  </Button>
};

export const QueryKanban = (props) => {
  const { boardData, columns, rows, onUpdateStatusClick, onAddNewCardClick, name, hasSubGroup } = props;
  if (hasSubGroup) {
    return <KanbanBoard name={name}>
      {Object.entries(boardData).map(([project, y]) =>
        <KanbanSwimlane name={project}>
        {columns.map(column =>
          <KanbanColumn name={column}>
          {y[column] &&  y[column].map(({ ...props}) =>
            <KanbanCard columns={columns} {...props} onUpdateStatusClick={onUpdateStatusClick} />
            )}
            /* AddCard needs to take a context object that accepts filters, row context, column context, etc. */
          <AddCardButton column={column} project={project} onAddNewCardClick={onAddNewCardClick} />
          </KanbanColumn>
          )}
        <AddColumnButton />
        </KanbanSwimlane>
        )}
    {/* <AddSwimlaneButton/> */}
    </KanbanBoard>
  } else {
    return <KanbanBoard name={name}>
      {Object.entries(boardData).map(([column, y]) =>
        <KanbanColumn name={column}>
        {y && y.map(({ ...props}) =>
          <KanbanCard columns={columns} {...props} onUpdateStatusClick={onUpdateStatusClick} />
          )}
        <AddCardButton column={column} onAddNewCardClick={onAddNewCardClick} />
        </KanbanColumn>
        )}
    </KanbanBoard>
  }

}
