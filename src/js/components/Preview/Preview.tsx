import { Box, Portal } from '@chakra-ui/react';

const targetIsPageLink = (e) => e?.target?.classList?.contains('link');
const targetIsUrlLink = (e) => e?.target?.classList?.contains('url-link');
const targetIsHashtag = (e) => e?.target?.classList?.contains('hashtag');
const targetIsAutolink = (e) => e?.target?.classList?.contains('autolink');

const attrIf = (e, condition, attr) => {
  if ((e.target) && condition(e)) return e.target.getAttribute(attr);
  return false;
};

export const InteractionManager = ({
  children,
  onNavigateUid,
  onNavigatePage,
  setPreviewPos,
  setPreview,
}) => {

  // When hovering a page, link, breadcrumb, etc.
  const handleMouseOver = (e) => {
    setPreviewPos({ x: e.clientX, y: e.clientY });

    const previewedPage = attrIf(e, targetIsPageLink, 'data-page-title');
    if (previewedPage) {
      setPreview(previewedPage, 'page');
      return;
    }

    const previewedHashtag = attrIf(e, targetIsHashtag, 'data-page-title');
    if (previewedHashtag) {
      setPreview(previewedHashtag, 'page');
      return;
    }

    const previewedUrl = attrIf(e, targetIsUrlLink, 'href');
    if (previewedUrl) {
      setPreview(previewedUrl, 'url');
      return;
    }

    const previewedAutoUrl = attrIf(e, targetIsAutolink, 'href');
    if (previewedAutoUrl) {
      console.log('found autolink');
      setPreview(previewedAutoUrl, 'url');
      return;
    }

    setPreview(null, null);
  }


  // When clicking a page, link, breadcrumb, etc.
  const handleClick = (e) => {

    const clickedPage = attrIf(e, targetIsPageLink, 'data-page-title');
    if (clickedPage) {
      onNavigatePage(clickedPage, e);
      return;
    }

    const clickedHashtag = attrIf(e, targetIsHashtag, 'data-page-title');
    if (clickedHashtag) {
      onNavigatePage(clickedHashtag, e);
      return;
    }

    // Anchor links
    const clickedUrlLink = attrIf(e, targetIsUrlLink, 'href');
    if (clickedUrlLink) {
      e.preventDefault();
      e.stopPropagation();
      window.history.pushState({}, '', clickedUrlLink);
      return;
    }

    const clickedAutoLink = attrIf(e, targetIsAutolink, 'href');
    if (clickedAutoLink) {
      e.preventDefault();
      e.stopPropagation();
      window.history.pushState({}, '', clickedAutoLink);
      return;
    }

  }

  return (
    <Box
      display="contents"
      onMouseOver={handleMouseOver}
      onScroll={handleMouseOver}
      onClick={handleClick}
    >
      {children}
    </Box>)
}

export const Preview = ({ isOpen, pos, children }) => {
  if (isOpen) {
    return <Portal>
      <Box
        pointerEvents="none"
        position="fixed"
        p={4}
        borderRadius="lg"
        shadow="popover"
        left={pos?.x + 10}
        top={pos?.y + 10}
        background="background.upper"
        height="20rem"
        width="20rem"
        overflow="hidden"
      >
        {children}
      </Box>
    </Portal>
  } else {
    return null;
  }
}