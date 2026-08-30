import React, { useState, useEffect } from 'react';
import {
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalBody,
  ModalFooter,
  ModalCloseButton,
  Button,
  ButtonGroup,
  Box,
  Flex,
  Heading,
  Text,
  Badge,
  HStack,
  VStack,
  Progress,
  IconButton,
} from '@chakra-ui/react';
import {
  SearchIcon,
  GraphIcon,
  CalendarEditFillIcon,
  AllPagesIcon,
  HelpIcon,
  ChevronRightIcon,
  ChevronLeftIcon,
  ArrowRightIcon,
} from '@/Icons/Icons';

export interface ProductDemoModalProps {
  isOpen: boolean;
  initialStep?: number;
  onClose: () => void;
  onComplete?: () => void;
  onSkip?: () => void;
}

interface DemoStep {
  id: string;
  badge: string;
  badgeColor: string;
  title: string;
  subtitle: string;
  description: string[];
  icon: React.ReactNode;
  demoSnippet?: {
    label: string;
    rawText: string;
    renderedPreview: React.ReactNode;
  };
  tips?: string[];
}

export const ProductDemoModal: React.FC<ProductDemoModalProps> = ({
  isOpen,
  initialStep = 0,
  onClose,
  onComplete,
  onSkip,
}) => {
  const [currentStep, setCurrentStep] = useState(initialStep);

  useEffect(() => {
    if (isOpen) {
      setCurrentStep(initialStep || 0);
    }
  }, [isOpen, initialStep]);

  const steps: DemoStep[] = [
    {
      id: 'welcome',
      badge: 'Welcome',
      badgeColor: 'teal',
      title: 'Welcome to Athens',
      subtitle: 'The open-source knowledge graph for networked thought.',
      icon: <GraphIcon boxSize={8} color="teal.400" />,
      description: [
        'Athens is designed to work the way human memory and reasoning work: by associating ideas across time and context.',
        'Unlike traditional hierarchical folders or rigid documents, Athens organizes your knowledge into a living, interconnected graph of atomic thoughts.',
        'This quick 2-minute tour walks you through the core concepts that unlock Athens\' superpowers.',
      ],
      demoSnippet: {
        label: 'The Philosophy of Networked Thought',
        rawText: '[[Networked Thought]] connects [[Ideas]] across [[Daily Notes]].',
        renderedPreview: (
          <Box p={3} borderRadius="md" bg="background.basement" border="1px solid" borderColor="separator.divider">
            <Text fontSize="sm">
              <Text as="span" color="highlight" fontWeight="semibold" textDecoration="underline">
                [[Networked Thought]]
              </Text>{' '}
              connects{' '}
              <Text as="span" color="highlight" fontWeight="semibold" textDecoration="underline">
                [[Ideas]]
              </Text>{' '}
              across{' '}
              <Text as="span" color="highlight" fontWeight="semibold" textDecoration="underline">
                [[Daily Notes]]
              </Text>
              .
            </Text>
          </Box>
        ),
      },
      tips: [
        'Everything is local-first: your notes stay on your machine.',
        'You can revisit this tour anytime from the Help (?) menu or toolbar.',
      ],
    },
    {
      id: 'daily-notes',
      badge: 'Capture',
      badgeColor: 'blue',
      title: 'Daily Notes: Frictionless Stream of Consciousness',
      subtitle: 'Never worry about where to store an idea or note.',
      icon: <CalendarEditFillIcon boxSize={8} color="blue.400" />,
      description: [
        'Every time you open Athens, you land on your Daily Notes page for today.',
        'Use Daily Notes as your scratchpad for daily standups, meeting notes, sudden epiphanies, reading highlights, and todos.',
        'Because pages are linked bidirectionally, you never have to decide on a perfect folder before you write.',
      ],
      demoSnippet: {
        label: 'Example Daily Note Entry',
        rawText: '- Met with [[Alex]] on [[Decentralized Storage]]\n  - {{[[TODO]]}} Review smart contract architecture\n  - Reference: [[Athens Roadmap]]',
        renderedPreview: (
          <VStack align="stretch" spacing={2} p={3} borderRadius="md" bg="background.basement" border="1px solid" borderColor="separator.divider">
            <HStack spacing={2}>
              <Box w="6px" h="6px" borderRadius="full" bg="foreground.secondary" />
              <Text fontSize="sm">
                Met with <Text as="span" color="highlight" fontWeight="semibold">[[Alex]]</Text> on <Text as="span" color="highlight" fontWeight="semibold">[[Decentralized Storage]]</Text>
              </Text>
            </HStack>
            <HStack spacing={2} pl={4}>
              <Badge colorScheme="purple" fontSize="xs">TODO</Badge>
              <Text fontSize="sm">Review smart contract architecture</Text>
            </HStack>
            <HStack spacing={2} pl={4}>
              <Box w="4px" h="4px" borderRadius="full" bg="foreground.secondary" />
              <Text fontSize="sm" color="foreground.secondary">
                Reference: <Text as="span" color="highlight" fontWeight="semibold">[[Athens Roadmap]]</Text>
              </Text>
            </HStack>
          </VStack>
        ),
      },
      tips: [
        'Scroll down in Daily Notes to effortlessly review previous days.',
      ],
    },
    {
      id: 'linking',
      badge: 'Connections',
      badgeColor: 'purple',
      title: 'Bi-Directional Linking with [[Page]]',
      subtitle: 'Build organic relationships without rigid hierarchies.',
      icon: <AllPagesIcon boxSize={8} color="purple.400" />,
      description: [
        'Type "[[" anywhere to link to an existing page or instantly create a new page.',
        'When Page A links to Page B, Page B automatically creates a backlink showing that Page A referenced it.',
        'Athens also automatically discovers "Unlinked References" — finding every place you mentioned a concept before creating its page.',
      ],
      demoSnippet: {
        label: 'Live Bi-directional Linking',
        rawText: 'Type: [[Artificial Intelligence]] or #research',
        renderedPreview: (
          <Box p={3} borderRadius="md" bg="background.basement" border="1px solid" borderColor="separator.divider">
            <Text fontSize="sm">
              Exploring breakthroughs in{' '}
              <Badge colorScheme="purple" variant="subtle" px={1.5} py={0.5} borderRadius="md">
                [[Artificial Intelligence]]
              </Badge>{' '}
              and{' '}
              <Badge colorScheme="cyan" variant="solid" px={1.5} py={0.5} borderRadius="md">
                #knowledge-graphs
              </Badge>
            </Text>
          </Box>
        ),
      },
      tips: [
        'Both [[Double Brackets]] and #tags create first-class page links.',
        'Click any link while holding Shift to open it side-by-side in the Right Sidebar!',
      ],
    },
    {
      id: 'blocks',
      badge: 'Modularity',
      badgeColor: 'orange',
      title: 'Atomic Blocks & References',
      subtitle: 'Every bullet point is an addressable, reusable unit of thought.',
      icon: <GraphIcon boxSize={8} color="orange.400" />,
      description: [
        'In Athens, each bullet point is an atomic block with a unique ID.',
        'You can reference a block anywhere using "((block-id))", ensuring you never have to duplicate text.',
        'Use block embeds "{{[[embed]]:((block-id))}}" to view and edit content simultaneously from multiple locations.',
      ],
      demoSnippet: {
        label: 'Block References vs Embeds',
        rawText: '((9c4a-81f2)) -> References block directly\n{{[[embed]]:((9c4a-81f2))}} -> Embeds live editable block',
        renderedPreview: (
          <VStack align="stretch" spacing={2} p={3} borderRadius="md" bg="background.basement" border="1px solid" borderColor="separator.divider">
            <HStack>
              <Badge colorScheme="orange">Block Ref</Badge>
              <Text fontSize="xs" color="foreground.secondary">((9c4a-81f2))</Text>
            </HStack>
            <Box pl={3} borderLeft="2px solid" borderColor="orange.400">
              <Text fontSize="sm" fontStyle="italic">"Knowledge grows exponentially when thoughts are deeply interconnected."</Text>
            </Box>
          </VStack>
        ),
      },
      tips: [
        'Press Tab to indent a block, Shift+Tab to unindent.',
        'Drag the bullet icon to reorder blocks effortlessly.',
      ],
    },
    {
      id: 'athena',
      badge: 'Navigation',
      badgeColor: 'green',
      title: 'Athena: Instant Search & Creation',
      subtitle: 'Lightning-fast fuzzy search across your entire second brain.',
      icon: <SearchIcon boxSize={8} color="green.400" />,
      description: [
        'Press Cmd+K (Mac) or Ctrl+K (Windows/Linux) to invoke the Athena command bar.',
        'Start typing to instantly fuzzy-search through all page titles and block contents.',
        'If a page doesn\'t exist, simply press Enter to create and navigate to it immediately.',
      ],
      demoSnippet: {
        label: 'Athena Command Bar Shortcut',
        rawText: 'Shortcut: Cmd + K / Ctrl + K',
        renderedPreview: (
          <HStack p={3} borderRadius="md" bg="background.basement" border="1px solid" borderColor="separator.divider" justify="space-between">
            <HStack spacing={3}>
              <SearchIcon color="green.400" />
              <Text fontSize="sm" color="foreground.secondary">Find or create page: <Text as="span" color="foreground.primary" fontWeight="bold">Athens Architecture</Text></Text>
            </HStack>
            <HStack spacing={1}>
              <Kbd>Cmd</Kbd>
              <Kbd>K</Kbd>
            </HStack>
          </HStack>
        ),
      },
      tips: [
        'Press Shift+Enter in Athena to open the search result directly in the Right Sidebar!',
      ],
    },
    {
      id: 'graph',
      badge: 'Visualization',
      badgeColor: 'cyan',
      title: 'Visual Knowledge Graph',
      subtitle: 'See your thoughts as a connected web of ideas.',
      icon: <GraphIcon boxSize={8} color="cyan.400" />,
      description: [
        'Click "Graph" in the left sidebar to open your full 2D interactive knowledge graph.',
        'Nodes represent pages, and lines represent bidirectional links connecting them.',
        'As your second brain grows, you will uncover serendipitous connections and clusters between seemingly disparate fields.',
      ],
      demoSnippet: {
        label: 'Interactive Knowledge Graph Highlights',
        rawText: 'Interactive physics, clusters, zoom/pan navigation',
        renderedPreview: (
          <Box p={4} borderRadius="md" bg="background.basement" border="1px solid" borderColor="separator.divider" textAlign="center">
            <HStack justify="center" spacing={4} py={2}>
              <Badge colorScheme="cyan" variant="solid" p={2} borderRadius="full">● Philosophy</Badge>
              <Text color="cyan.400">──────</Text>
              <Badge colorScheme="purple" variant="solid" p={2} borderRadius="full">● Epistemology</Badge>
              <Text color="purple.400">──────</Text>
              <Badge colorScheme="teal" variant="solid" p={2} borderRadius="full">● Athens</Badge>
            </HStack>
            <Text fontSize="xs" color="foreground.secondary" mt={2}>
              Click and drag nodes to explore relationship clusters.
            </Text>
          </Box>
        ),
      },
      tips: [
        'Use the Graph Settings panel to filter orphans or adjust link distances and charge strength.',
      ],
    },
    {
      id: 'ready',
      badge: 'Get Started',
      badgeColor: 'yellow',
      title: 'You\'re Ready to Build Your Knowledge Graph!',
      subtitle: 'Athens is ready for your thoughts, research, and discoveries.',
      icon: <HelpIcon boxSize={8} color="yellow.400" />,
      description: [
        'You now know the core mechanics of Athens:',
        '1. Capture daily in Daily Notes',
        '2. Link concepts using [[double brackets]] and #tags',
        '3. Reuse blocks using ((references)) and embeds',
        '4. Search and create instantly with Cmd/Ctrl + K',
        '5. Explore emergent clusters in the Graph View',
      ],
      demoSnippet: {
        label: 'Key Cheatsheet',
        rawText: '[[Page]] | ((Block)) | Cmd+K (Athena) | / (Slash Menu) | ? (Help)',
        renderedPreview: (
          <HStack justify="space-around" p={3} borderRadius="md" bg="background.basement" border="1px solid" borderColor="separator.divider" flexWrap="wrap" gap={2}>
            <HStack><Kbd>[[</Kbd><Text fontSize="xs">Link</Text></HStack>
            <HStack><Kbd>((</Kbd><Text fontSize="xs">Block Ref</Text></HStack>
            <HStack><Kbd>Cmd+K</Kbd><Text fontSize="xs">Athena</Text></HStack>
            <HStack><Kbd>/</Kbd><Text fontSize="xs">Commands</Text></HStack>
            <HStack><Kbd>?</Kbd><Text fontSize="xs">Cheatsheet</Text></HStack>
          </HStack>
        ),
      },
      tips: [
        'Need syntax help? Click the Help (?) icon in the top right or press ? anytime.',
      ],
    },
  ];

  const totalSteps = steps.length;
  const currentStepData = steps[currentStep] || steps[0];
  const isFirstStep = currentStep === 0;
  const isLastStep = currentStep === totalSteps - 1;
  const progressPercent = ((currentStep + 1) / totalSteps) * 100;

  const handleNext = () => {
    if (isLastStep) {
      if (onComplete) onComplete();
      onClose();
    } else {
      setCurrentStep((prev) => Math.min(prev + 1, totalSteps - 1));
    }
  };

  const handlePrev = () => {
    setCurrentStep((prev) => Math.max(prev - 1, 0));
  };

  const handleSkip = () => {
    if (onSkip) onSkip();
    if (onComplete) onComplete();
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleSkip}
      isCentered
      size="xl"
      scrollBehavior="inside"
    >
      <ModalOverlay backdropFilter="blur(4px)" bg="blackAlpha.600" />
      <ModalContent
        bg="background.floor"
        color="foreground.primary"
        borderRadius="xl"
        boxShadow="2xl"
        border="1px solid"
        borderColor="separator.divider"
        maxW="680px"
        overflow="hidden"
      >
        {/* Header with Progress Bar */}
        <Box>
          <Progress
            value={progressPercent}
            size="xs"
            colorScheme="teal"
            bg="background.basement"
          />
          <ModalHeader pt={5} pb={2}>
            <Flex justify="space-between" align="center">
              <HStack spacing={2.5}>
                {currentStepData.icon}
                <Box>
                  <HStack spacing={2} mb={1}>
                    <Badge colorScheme={currentStepData.badgeColor} variant="subtle" fontSize="xs" px={2} py={0.5} borderRadius="full">
                      {currentStepData.badge}
                    </Badge>
                    <Text fontSize="xs" color="foreground.secondary" fontWeight="medium">
                      Step {currentStep + 1} of {totalSteps}
                    </Text>
                  </HStack>
                  <Heading size="md" fontWeight="bold">
                    {currentStepData.title}
                  </Heading>
                </Box>
              </HStack>
              <ModalCloseButton position="static" />
            </Flex>
          </ModalHeader>
        </Box>

        <ModalBody py={4}>
          <VStack align="stretch" spacing={4}>
            <Text fontSize="md" color="foreground.secondary" fontWeight="medium">
              {currentStepData.subtitle}
            </Text>

            {/* Explanatory bullet points */}
            <VStack align="stretch" spacing={2.5} pl={1}>
              {currentStepData.description.map((paragraph, index) => (
                <HStack key={index} align="flex-start" spacing={2.5}>
                  <Box
                    mt={1.5}
                    w="5px"
                    h="5px"
                    borderRadius="full"
                    bg="teal.400"
                    flexShrink={0}
                  />
                  <Text fontSize="sm" lineHeight="tall">
                    {paragraph}
                  </Text>
                </HStack>
              ))}
            </VStack>

            {/* Interactive / Visual Snippet */}
            {currentStepData.demoSnippet && (
              <Box mt={2}>
                <Text fontSize="xs" fontWeight="semibold" textTransform="uppercase" letterSpacing="wider" color="foreground.secondary" mb={1.5}>
                  {currentStepData.demoSnippet.label}
                </Text>
                {currentStepData.demoSnippet.renderedPreview}
              </Box>
            )}

            {/* Helpful Pro-Tips */}
            {currentStepData.tips && currentStepData.tips.length > 0 && (
              <Box
                p={3}
                borderRadius="md"
                bg="background.vibrancy"
                borderLeft="3px solid"
                borderLeftColor="teal.400"
              >
                <VStack align="stretch" spacing={1}>
                  {currentStepData.tips.map((tip, idx) => (
                    <Text key={idx} fontSize="xs" color="foreground.secondary">
                      <Text as="span" fontWeight="bold" color="teal.300">Tip: </Text>
                      {tip}
                    </Text>
                  ))}
                </VStack>
              </Box>
            )}
          </VStack>
        </ModalBody>

        <ModalFooter
          borderTop="1px solid"
          borderColor="separator.divider"
          bg="background.basement"
          py={3}
          px={6}
        >
          <Flex justify="space-between" align="center" w="100%">
            {/* Step indicator dots */}
            <HStack spacing={1.5}>
              {steps.map((_, idx) => (
                <Box
                  key={idx}
                  w={idx === currentStep ? '18px' : '6px'}
                  h="6px"
                  borderRadius="full"
                  bg={idx === currentStep ? 'teal.400' : 'foreground.secondary'}
                  opacity={idx === currentStep ? 1 : 0.3}
                  cursor="pointer"
                  transition="all 0.2s ease"
                  onClick={() => setCurrentStep(idx)}
                />
              ))}
            </HStack>

            {/* Action buttons */}
            <ButtonGroup size="sm" spacing={2}>
              {!isLastStep && (
                <Button variant="ghost" colorScheme="subtle" onClick={handleSkip}>
                  Skip Tour
                </Button>
              )}
              {!isFirstStep && (
                <IconButton
                  aria-label="Previous Step"
                  icon={<ChevronLeftIcon />}
                  variant="outline"
                  onClick={handlePrev}
                />
              )}
              <Button
                colorScheme="teal"
                onClick={handleNext}
                rightIcon={isLastStep ? <ArrowRightIcon /> : <ChevronRightIcon />}
              >
                {isLastStep ? 'Start Taking Notes' : 'Next Step'}
              </Button>
            </ButtonGroup>
          </Flex>
        </ModalFooter>
      </ModalContent>
    </Modal>
  );
};

const Kbd: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <Box
    as="kbd"
    px={2}
    py={0.5}
    fontSize="xs"
    fontWeight="semibold"
    borderRadius="md"
    bg="background.floor"
    border="1px solid"
    borderColor="separator.divider"
    boxShadow="xs"
  >
    {children}
  </Box>
);
