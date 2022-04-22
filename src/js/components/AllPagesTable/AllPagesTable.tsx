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
      maxWidth="75rem"
      marginLeft="auto"
      marginRight="auto"
      display="flex"
      sx={{
        left: "auto !important"
      }}
      className={index % 2 ? 'index-even' : 'index-odd'}
    >
      <Td
        overflow="hidden"
      >
        <Button
          onClick={data[index].onClick}
          variant="link"
          color="link"
          width="100%"
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
    sx={{
      "--margin-top": "2rem",
    }}
    paddingTop="calc(var(--app-header-height) + var(--margin-top))"
    width="100%"
    height="100%"
  >
    <Table variant="striped"
      height="100%"
      sx={{
        "tr > *:nth-child(1)": {
          flex: "0 0 calc(100% - 36rem)"
        },
        "tr > *:nth-child(2)": {
          flex: "0 0 12rem"
        },
        "tr > *:nth-child(3)": {
          flex: "0 0 12rem"
        },
        "tr > *:nth-child(4)": {
          flex: "0 0 12rem"
        },
      }}
    >
      <Thead>
        <Tr
          display="flex"
          width="100%"
          maxWidth="75rem"
          margin="auto"
        >
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
        display="flex"
        width="100%"
        height="100%"
        sx={{
          // target the container that renders the row items
          "> div > div": {
            display: "flex",
            justifyContent: "center",
          }
        }}
      >
        <List
          height={containerHeight}
          itemCount={rows.length}
          itemSize={52}
          itemData={rows}
          style={{
            position: "absolute",
            top: 0,
            left: "auto",
            right: "auto",
            width: "100%",
          }}
        >
          {Row}
        </List>
      </Tbody>
    </Table>
  </Box >
}
