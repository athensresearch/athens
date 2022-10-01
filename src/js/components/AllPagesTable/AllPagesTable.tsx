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


// react-window adds style props to the children, but
// we don't want all of them, and we'd rather not
// override them below.
const removedStyleKeys = ['left', 'right', 'width'];
const filterStyle = (style) => Object.keys(style).reduce((acc, key) => {
  if (!removedStyleKeys.includes(key)) {
    acc[key] = style[key];
  }
  return acc;
}, {});


const renderDate = (date) => {
  if (typeof date === 'string') {
    return date
  } else {
    return "—"
  }
}

const RowTd = ({ children, ...props }) => {
  return (
    <Td {...props}
      transitionProperty="background"
      transitionTimingFunction="ease-in-out"
      transitionDuration="fast"
    >
      {children}
    </Td>
  )
}

const Row = ({ index, data, style }) => {

  const item = data[index];

  return (
    <Tr
      style={filterStyle(style)}
      width="var(--child-width)"
      maxWidth="var(--max-child-width)"
      marginLeft="auto"
      marginRight="auto"
      display="flex"
      className={index % 2 ? 'index-even' : 'index-odd'}
    >
      <RowTd
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
          {DISPLAY_TITLES[item[":node/title"]] || item[":node/title"]}
        </Button>
      </RowTd>
      <RowTd>{item[":block/_refs"]?.length || 0}</RowTd>
      <RowTd>{renderDate(item[":time/modified"])}</RowTd>
      <RowTd>{renderDate(item[":time/created"])}</RowTd>
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

export const AllPagesTable = ({ sortedPages, onClickItem, sortedBy, sortDirection, onClickSort, dateFormatFn }) => {
  const containerRef = React.useRef();
  const [containerHeight, setContainerHeight] = React.useState(500);
  const columns = ['title', 'links-count', 'modified', 'created'];
  const rows = React.useMemo(() => sortedPages.map((row) => {
    return {
      ...row,
      onClick: (e) => onClickItem(e, row[":node/title"]),
      ":time/modified": dateFormatFn(row[":time/modified"]),
      ":time/created": dateFormatFn(row[":time/created"]),
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
    flex="1 1 100%"
    alignSelf="stretch"
    height="var(--app-height)"
    px={4}
    sx={{
      "--margin-top": "2rem",
      "--thead-height": "8rem",
      "--child-width": "75rem",
      "--max-child-width": "max(100vw - 4rem)",
    }}
  >
    <Table variant="striped"
      height="var(--app-height)"
      sx={{
        "tr > *:nth-of-type(1)": {
          flex: "0 0 calc(100% - 39rem)"
        },
        "tr > *:nth-of-type(2)": {
          flex: "0 0 7rem",
          color: "foreground.secondary",
          fontSize: "sm"
        },
        "tr > *:nth-of-type(3)": {
          flex: "0 0 16rem",
          color: "foreground.secondary",
          fontSize: "sm"
        },
        "tr > *:nth-of-type(4)": {
          flex: "0 0 16rem",
          color: "foreground.secondary",
          fontSize: "sm"
        },
      }}
    >
      <Thead>
        <Tr
          display="flex"
          width="var(--child-width)"
          maxWidth="var(--max-child-width)"
          margin="auto"
        >
          {columns.map((column, index) => {
            return <Th
              key={index}
              height="var(--thead-height)"
            >
              <Button
                onClick={() => onClickSort(column)}
                size="sm"
                variant="link"
                height="1em"
                marginTop="calc(var(--thead-height) - 3em)"
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
        height="calc(var(--app-height) - var(--thead-height))"
        ref={containerRef}
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
            paddingBottom: "2rem",
          }}
        >
          {Row}
        </List>
      </Tbody>
    </Table>
  </Box >
}
