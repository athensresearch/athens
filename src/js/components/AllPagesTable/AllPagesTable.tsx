import React from 'react';
import { ChevronDownIcon, ChevronUpIcon } from '@/Icons/Icons';
import { FixedSizeList as List } from 'react-window';
import { Button, Box, Table, Thead, Tbody, Th, Td, Tr } from '@chakra-ui/react';

const DISPLAY_TITLES = {
  'title': 'Title',
  'links-count': 'Links',
  'modified': 'Modified',
  'created': 'Created',
}


const Row = ({ index, data, style }) => {
  const item = data[index];
  return (
    <Tr
      style={style}
      width="100%"
      display="flex"
      className={index % 2 ? 'index-even' : 'index-odd'}
    >
      <Td>
        <Button
          onClick={data[index].onClick}
          variant="link"
          color="link"
          display="block"
          justifyContent="flex-start"
          whiteSpace="nowrap"
          textOverflow="ellipsis"
          overflow="hidden"
          textAlign="left"
          padding="0"
        >
          {DISPLAY_TITLES[item.title] || item.title}
        </Button>
      </Td>
      <Td>{item._refs?.length || 0}</Td>
      <Td>{item.time}</Td>
      <Td>test</Td>
    </Tr>
  )
};

const getHeight = (el) => {
  if (el) {
    return el.offsetHeight;
  } else {
    return 500;
  }
}

export const AllPagesTable = ({ sortedPages, onClickItem, sortedBy, sortDirection, onClickSort }) => {
  const containerRef = React.useRef();
  const [containerHeight, setContainerHeight] = React.useState(500);
  const columns = ['title', 'links-count', 'modified', 'created'];
  const rows = React.useMemo(() => sortedPages.map((row, index) => {
    return {
      ...row,
      onClick: (e) => onClickItem(e, row.title)
    }
  }), [sortedPages]);

  // Watch the window for resizing
  React.useLayoutEffect(() => {
    const updateSize = () => {
      setContainerHeight(getHeight(containerRef.current))
    };
    window.addEventListener("resize", updateSize);
    updateSize();
    return () => window.removeEventListener("resize", updateSize);
  })

  return <Box
    ref={containerRef}
    px={4}
    width="100%"
    maxWidth="75rem"
    margin="calc(var(--app-header-height) + 2rem) auto 5rem"
    height="calc(100% - var(--app-header-height) - 6rem)"
  >
    <Table variant="striped"
      sx={{
        "tr > *:nth-child(1)": {
          flex: "0 0 calc(100% - 30rem)"
        },
        "tr > *:nth-child(2)": {
          flex: "0 0 10rem"
        },
        "tr > *:nth-child(3)": {
          flex: "0 0 10rem"
        },
        "tr > *:nth-child(4)": {
          flex: "0 0 10rem"
        },
      }}
    >
      <Thead>
        <Tr display="flex">
          {columns.map((column, index) => {
            return <Th key={index}>
              <Button
                onClick={() => onClickSort(column)}
                size="sm"
                variant="link"
                height="1em"
                justifyContent="flex-start"
                display="flex"
                rightIcon={
                  sortedBy === column ?
                    sortDirection === 'asc' ?
                      <ChevronUpIcon /> :
                      <ChevronDownIcon /> :
                    null
                }
              >
                {DISPLAY_TITLES[column] || column}
              </Button>
            </Th>
          })}
        </Tr>
      </Thead>
      <Tbody
        position="relative"
        width="100%"
      >
        <List
          height={containerHeight}
          itemCount={rows.length}
          itemSize={52}
          itemData={rows}
          style={{
            position: "absolute",
            top: 0,
            left: 0,
            width: "100%",
          }}
          width="100%"
        >
          {Row}
        </List>
      </Tbody>
    </Table>
  </Box >
}
