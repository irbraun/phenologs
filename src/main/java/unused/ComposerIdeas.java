
package unused;







public class ComposerIdeas {
    
    
    /*
    private final Text text;
    private final HashMap<Ontology,Onto> ontoObjects;
    private final RulesForPATO patoInfo;
    private final HashMap<Integer,ArrayList<Term>> qTermProbabilityTable;
    private final HashMap<Integer,ArrayList<Term>> eTermProbabilityTable;
    private final HashMap<Integer,Integer> splitMap;
    private final Parser parser;
    */
    
    
    
    public ComposerIdeas(){

        /*
        text = new Text();
        ontoObjects = new HashMap<>();
        ontoObjects.put(Ontology.PATO, new Onto(Config.ontologyPaths.get(Ontology.PATO)));
        ontoObjects.put(Ontology.PO, new Onto(Config.ontologyPaths.get(Ontology.PO)));
        ontoObjects.put(Ontology.GO, new Onto(Config.ontologyPaths.get(Ontology.GO)));
        ontoObjects.put(Ontology.CHEBI, new Onto(Config.ontologyPaths.get(Ontology.CHEBI)));
        splitMap = new HashMap<>();
        InfoContent.setup(ontoObjects, text);
        
        // PATO specific info contains which terms are optional qualifiers or relational qualities.
        patoInfo = new RulesForPATO(ontoObjects.get(Ontology.PATO));
        
        logger.info("reading class probabilities");
        List<HashMap<Integer,ArrayList<Term>>> entityClassProbsMaps = new ArrayList<>();
        entityClassProbsMaps.add(readClassProbFiles(Ontology.PO, Config.classProbsPaths.get(Ontology.PO)));
        entityClassProbsMaps.add(readClassProbFiles(Ontology.GO, Config.classProbsPaths.get(Ontology.GO)));
        entityClassProbsMaps.add(readClassProbFiles(Ontology.CHEBI, Config.classProbsPaths.get(Ontology.CHEBI)));
        eTermProbabilityTable = mergeClassProbFiles(entityClassProbsMaps);
        qTermProbabilityTable = readClassProbFiles(Ontology.PATO, Config.classProbsPaths.get(Ontology.PATO));

        logger.info("running nlp pipeline");
        List<Integer> chunkIDs = new ArrayList<>(qTermProbabilityTable.keySet());
        List<Chunk> chunks = text.getDefaultChunksFromChunkIDs(chunkIDs);
        parser = new Parser();
        parser.loadAnnotations(chunks);
        parser.loadDistributions(Config.distributionsPath);

        
        logger.info("predicting eq statements");
        makeTable();
        */
    }
    
    
    
    

    /*
    private void makeTable() throws FileNotFoundException, ClassExpressionException, SQLException, NonExistingEntityException, Exception{
        File outputFile = new File(Config.predictedStmtsPath);
        PrintWriter writer = new PrintWriter(outputFile);
        switch(utils.Util.inferTextType(Config.format)){
        case PHENOTYPE:
            makeOutputTablePh(writer);
            break;
        case ATOM:
            makeOutputTableAtStmt(writer);
            break;
        case PREDICTED_ATOM:
            makeOutputTablePredAtStmt(writer);
            break;
        }
        writer.close();
    }
    */
        
    
    
    
    
    
    
    
            
           
    
    /**
     * Produces the output data when the class probability tables were generated using phenotype IDs mapped to ontology terms
     * and probabilities. The output table includes similarity scores between curated EQ statements for phenotypes and predicted
     * EQ statements for phenotypes. For each predicted EQ statement, the curated atomized statement and EQ statement that mazimize
     * EQ statement to EQ statement similarity are also shown on that line, and the similarity reported, for illustrative purposes.
     * In this case, that atomized statement was not actually used in any way to generate any of the predicted EQ statements.
     * The only input to the actual predictive process was the phenotype description text, so the phenotype similarity score is the 
     * most relevant.
     * 
     * Important assumption for this case:
     * Class probabilities must be specified using phenotype IDs in the input files.
     * 
     * 
     * @param writer
     * @throws ClassExpressionException
     * @throws SQLException
     * @throws NonExistingEntityException 
     */
    /*
    private void makeOutputTablePh(PrintWriter writer) throws ClassExpressionException, SQLException, NonExistingEntityException, Exception{
        
        writer.println("phenotype_id, "
                + "phenotype_text, "
                + "atomized_statement_id, "
                + "atomized_statement_text, "
                + "curated_eq_labels, "
                + "curated_eq_ids, "
                + "predicted_eq_labels, "
                + "predicted_eq_ids, "
                + "p1, "
                + "p2, "
                + "q, "
                + "atomized_statement_sim, "
                + "phenotype_sim");
                
        HashSet<Integer> phenotypeIDs = new HashSet<>(qTermProbabilityTable.keySet());
        
        for (int phenotypeID: phenotypeIDs){

            ArrayList<Integer> curatedAtomIDs = text.getAtomIDsFromPhenotypeID(phenotypeID);
            ArrayList<EQStatement> curatedEQs = text.getCuratedEQStatementsFromAtomIDs(curatedAtomIDs);
            ArrayList<EQStatement> predictedEQs = getPredictedEQs(phenotypeID);
            
            // Calculate the values of q for each predicted statement and apply threshold.
            predictedEQs = assignQScores(predictedEQs, Config.beta.get(splitMap.get(phenotypeID)));
            predictedEQs = applyQThreshold(predictedEQs, Config.qThreshold.get(splitMap.get(phenotypeID)));

            
            // Now get the phenotype similarity between all the curatedEQs and the predictedEQs.
            double phenotypeSimilarity = Utils.getSimilarity(predictedEQs, curatedEQs, ontoObjects);
            
            // Iterate through the predicted EQ statements for this phenotype.
            for (EQStatement predictedEQ: predictedEQs){
                
                // Maximize the similarity to a curated EQ statement and track of that EQ statement's atom ID. Purely illustrative.
                double maxSimilarity = 0.00;
                int maxAtomID = -1;
                for (int curatedAtomID: curatedAtomIDs){
                    EQStatement curatedEQ = text.getCuratedEQStatementFromAtomID(curatedAtomID);
                    double similarity = Utils.getSimilarity(predictedEQ, curatedEQ, ontoObjects);
                    if (similarity >= maxSimilarity){
                        maxSimilarity = similarity;
                        maxAtomID = curatedAtomID;
                    }
                }

                Object[] data = {phenotypeID, 
                                text.getPhenotypeDescStr(phenotypeID).replace(",", ""), 
                                maxAtomID,
                                text.getAtomizedStatementStr(maxAtomID).replace(",", ""), 
                                text.getCuratedEQStatementFromAtomID(maxAtomID).toLabelText(ontoObjects).replace(",", ""),
                                text.getCuratedEQStatementFromAtomID(maxAtomID).toIDText().replace(",", ""),
                                predictedEQ.toLabelText(ontoObjects).replace(",", ""),
                                predictedEQ.toIDText().replace(",", ""),
                                predictedEQ.termScore,
                                //predictedEQ.p2Score,
                                "dummy p2 score",
                                predictedEQ.coverage,
                                maxSimilarity,
                                phenotypeSimilarity
                };          
                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", data));
            }
        }
    }
    */
    
    
    
    
    
    
    /**
     * Produces the output table when the class probability tables were generated using atomized statement IDs mapped
     * to ontology terms and probabilities. The source of the predictions made for the candidate ontology terms came from
     * the atomized statements that were curated and already available in the tagged data set used for training and testing.
     * Therefore, the similarities between the curated EQ statements and the predicted EQ statements are very relevant
     * because they are one to one to each other. Those EQ statements come from the exact same text that was used to predict
     * the ontology terms. The phenotype to phenotype similarity is still included, in cases where all of the curated atomized
     * statements for a given phenotype were used as input for this testing set. To achieve this, the original testing/training
     * split has to be drawn from random phenotype (keep all atomized statements together), so that all or none of the
     * atomized statements for a given phenotype wind up in the testing set from which the class probability file is drawn.
     * 
     * Important assumption for this case:
     * Class probabilities must be specified using atomized statement IDs in the input files.
     * 
     * @param writer
     * @throws SQLException
     * @throws NonExistingEntityException
     * @throws ClassExpressionException 
     */
    /*(
    private void makeOutputTableAtStmt(PrintWriter writer) throws SQLException, NonExistingEntityException, ClassExpressionException, Exception{
        
        writer.println("phenotype_id, "
                + "phenotype_text, "
                + "atomized_statement_id, "
                + "atomized_statement_text, "
                + "curated_eq_labels, "
                + "curated_eq_ids, "
                + "predicted_eq_labels, "
                + "predicted_eq_ids, "
                + "p1, "
                + "p2_1, p2_2, p2_3, p2_4,"  // updating this to test multiple distributions.
                + "q, "
                + "atomized_statement_sim, "
                + "phenotype_sim");
        
        // We want to be able to iterate over phenotype IDs instead of atomized statement IDs.
        HashSet<Integer> atomIDs = new HashSet<>(qTermProbabilityTable.keySet());
        HashSet<Integer> phenotypeIDs = new HashSet<>();
        for (int atomID: atomIDs){
            int phenotypeID = text.getPhenotypeIDfromAtomID(atomID);
            phenotypeIDs.add(phenotypeID);
        }
        
        
        // loop through phenotypes found in the predicted data.
        for (int phenotypeID: phenotypeIDs){
            
            ArrayList<String> dataRowsForThisPhenotype = new ArrayList<>();
            ArrayList<Integer> curatedAtomIDs = text.getAtomIDsFromPhenotypeID(phenotypeID);
            
            // Check if all the curated atomized statements for this phenotype were predicted on.
            if (atomIDs.containsAll(curatedAtomIDs)){
                
                ArrayList<EQStatement> allCuratedEQsForThisPhenotype = new ArrayList<>();
                ArrayList<EQStatement> allPredictedEQsForThisPhenotype = new ArrayList<>();
                
                for (int curatedAtomID: curatedAtomIDs){
                    

                    // per atom output!
                    logger.info(curatedAtomID);
                    
                    
                    
                    
                    EQStatement curatedEQ = text.getCuratedEQStatementFromAtomID(curatedAtomID);
                    ArrayList<EQStatement> predictedEQs = getPredictedEQs(curatedAtomID);
                    
                    // Calculate the values of q for each predicted statement and apply threshold.
                    predictedEQs = assignQScores(predictedEQs, Config.beta.get(splitMap.get(curatedAtomID)));
                    predictedEQs = applyQThreshold(predictedEQs, Config.qThreshold.get(splitMap.get(curatedAtomID)));
                    
                    // Iterate through the predicted EQ statements.
                    for (EQStatement predictedEQ: predictedEQs){
                        
 
                        double similarity = Utils.getSimilarity(predictedEQ, text.getCuratedEQStatementFromAtomID(curatedAtomID), ontoObjects);

                        // Things that belong in the table (minus phenotype similarity).
                        Object[] data = {phenotypeID, 
                                        text.getPhenotypeDescStr(phenotypeID).replace(",", ""),
                                        curatedAtomID,
                                        text.getAtomizedStatementStr(curatedAtomID).replace(",", ""), 
                                        text.getCuratedEQStatementFromAtomID(curatedAtomID).toLabelText(ontoObjects).replace(",", ""),
                                        text.getCuratedEQStatementFromAtomID(curatedAtomID).toIDText().replace(",", ""),
                                        predictedEQ.toLabelText(ontoObjects).replace(",", ""),
                                        predictedEQ.toIDText().replace(",", ""),
                                        predictedEQ.termScore,
                                        //predictedEQ.p2ScoresString,
                                        predictedEQ.coverage,
                                        similarity,
                        };          
                        dataRowsForThisPhenotype.add(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", data));
                    }

                    // The growing lists of all the terms for the phenotype as a whole.
                    allPredictedEQsForThisPhenotype.addAll(predictedEQs);
                    allCuratedEQsForThisPhenotype.add(curatedEQ);

                }  
                // done with all the atoms for this phenotype, find the phenotype sim before looping to a new phenotype.
                double phenotypeSimilarity = Utils.getSimilarity(allPredictedEQsForThisPhenotype, allCuratedEQsForThisPhenotype, ontoObjects);   
                for (String dataRow: dataRowsForThisPhenotype){
                    writer.println(String.format("%s,%s",dataRow,phenotypeSimilarity));
                }
            }
            else {
                // Should never actually see this messages, should throw an exception here. Kept for testing purposes only.
                // This means that the partitioning didn't work correctly, discrepency between the original text data and upstream outputs.
                logger.info("some atomized statements missing for phenotype ID=" + phenotypeID);
            }  
        } 
    }
    */
    
    
    
    
    
    
    /**
     * This version of the output table is for when the class probability files are specified as pairs of ID's for the predicted
     * atomized statements (atomized statements achieved through some other process other than the curation) and ontology terms
     * and their probabilities. The predicted atomized statements are present in the output file in addition to the predicted EQ
     * statements like normal. For each predicted atomized statement there are one or more predicted EQ statement which have some 
     * quality score. For each predicted EQ statement, the curated EQ statement that has maximum similarity to it is displayed, along
     * with the corresponding atomized statement. This is just for illustrative purposes, to show in human-readable terms how the 
     * well the predicted atomized statements and the curated ones are matching up.
     * 
     * Note here: phenotype to phenotype (curated to predicted) similarity is measured as usual through the union of EQ statements 
     * irregardless of how many of the curated atomized statements are really being reflected by the predicted EQ statments. One 
     * alternative metric to report might be the sim(P1,P2) if only the curated EQ statements coming from atomized statements whose
     * EQ statements had maximal matches in the set of predicted ones for this curated phenotype. That should make the measure of 
     * similarity increase, so if using that it would be important to report both and explain what the difference was.
     * 
     * Assumptions for this case:
     * The Text object called text has to have structures that associate the predicted atomized statement's ID's to the phenoypte ID's 
     * and vice versa. 
     * The text ID's specifed in the class probability file have to come from the ID's of the predicted atomized statements.
     * 
     * 
     * @param writer 
     */
    /*
    private void makeOutputTablePredAtStmt(PrintWriter writer) throws ClassExpressionException, SQLException, NonExistingEntityException, Exception{
        
        writer.println("phenotype_id, "
                + "phenotype_text, "
                + "predicted_atomized_statement_id, "
                + "predicted_atomized_statement_text, "
                + "atomized_statement_id, "
                + "atomized_statement_text, "
                + "curated_eq_labels, "
                + "curated_eq_ids, "
                + "predicted_eq_labels, "
                + "predicted_eq_ids, "
                + "p1, "
                + "p2, "
                + "q, "
                + "atomized_statement_sim, "
                + "phenotype_sim");
        
        // We want to be able to iterate over phenotype IDs instead of predicted atomized statement IDs.
        HashSet<Integer> inFilePredAtomIDs = new HashSet<>(qTermProbabilityTable.keySet());
        HashSet<Integer> phenotypeIDs = new HashSet<>();
        for (int predAtomID: inFilePredAtomIDs){
            int phenotypeID = text.getPhenotypeIDfromPredAtomID(predAtomID);
            phenotypeIDs.add(phenotypeID);
        }
        
        // loop through phenotypes found in the predicted data.
        for (int phenotypeID: phenotypeIDs){
            
            ArrayList<String> dataRowsForThisPhenotype = new ArrayList<>();
            
            // These are the predicted atomized statement ID's that belong to this phenotype (as observed in the original text data structure).
            ArrayList<Integer> predAtomIDs = text.getPredAtomIDsFromPhenotypeID(phenotypeID);
            
            // Check if all the predicted atoms for this phenotype are present in the class probabilities file.
            if (inFilePredAtomIDs.containsAll(predAtomIDs)){
                
                ArrayList<EQStatement> allCuratedEQsForThisPhenotype = text.getCuratedEQStatementsFromAtomIDs(text.getAtomIDsFromPhenotypeID(phenotypeID));
                ArrayList<EQStatement> allPredictedEQsForThisPhenotype = new ArrayList<>();

                for (int predAtomID: predAtomIDs){
                    
                    ArrayList<EQStatement> predictedEQs = getPredictedEQs(predAtomID);
                    
                    // Calculate the values of q for each predicted statement and apply threshold.
                    predictedEQs = assignQScores(predictedEQs, Config.beta.get(splitMap.get(predAtomID)));
                    predictedEQs = applyQThreshold(predictedEQs, Config.qThreshold.get(splitMap.get(predAtomID)));
                    
                    // Iterate through the predicted EQ statements.
                    for (EQStatement predictedEQ: predictedEQs){
                    
                        // Find the curated EQ statement (and corresponding curated atom ID) that best match this predicted EQ statement.
                        double maxSimilarity = 0.00;
                        int maxCuratedAtomID = -1;
                        for (int curatedAtomID: text.getAtomIDsFromPhenotypeID(phenotypeID)){
                            
                            EQStatement curatedEQ = text.getCuratedEQStatementFromAtomID(curatedAtomID);
                            double similarity = Utils.getSimilarity(predictedEQ, curatedEQ, ontoObjects);
                            if (similarity >= maxSimilarity){
                                maxSimilarity = similarity;
                                maxCuratedAtomID = curatedAtomID;
                            }
                        }
                        
                        // Things that belong in the table.
                        Object[] data = {phenotypeID, 
                                        text.getPhenotypeDescStr(phenotypeID), 
                                        predAtomID,
                                        text.getPredictedAtomizedStatementString(predAtomID),
                                        maxCuratedAtomID,
                                        text.getAtomizedStatementStr(maxCuratedAtomID), 
                                        text.getCuratedEQStatementFromAtomID(maxCuratedAtomID).toLabelText(ontoObjects),
                                        text.getCuratedEQStatementFromAtomID(maxCuratedAtomID).toIDText(),
                                        predictedEQ.toLabelText(ontoObjects),
                                        predictedEQ.toIDText(),
                                        predictedEQ.termScore,
                                        //predictedEQ.p2Score,
                                        "dummy p2 score",
                                        predictedEQ.coverage,
                                        maxSimilarity
                        };          
                        dataRowsForThisPhenotype.add(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", data));
                        
                    }
                    
                    // The growing lists of all the terms for the phenotype as a whole.
                    allPredictedEQsForThisPhenotype.addAll(predictedEQs);

                }  
                // done with all the atoms for this phenotype, find the phenotype sim before looping to a new phenotype.
                double phenotypeSimilarity = Utils.getSimilarity(allPredictedEQsForThisPhenotype, allCuratedEQsForThisPhenotype, ontoObjects);   
                for (String dataRow: dataRowsForThisPhenotype){
                    writer.print(String.format("%s,%s",dataRow,phenotypeSimilarity));
                }
            }
            else {
                logger.info("some predicted atomized statements were missing for phenotype ID=" + phenotypeID);
            }  
        } 
        
    }
    */
    
    
    
    
    
    
    
    
    
    

    

    
    
    
    
    /**
     * Returns a list of candidate EQ statements based on the candidate terms which were provided in the input files,
     * and the average probability assigned to each of the terms for this text. These candidate terms are generated
     * ignoring any other potential source of information like structural similarity to the input text, etc. The 
     * provided chunkID can be of any type like a phenotype ID, atomized statement ID, or predicted atomized statement
     * ID, but it has to correspond with what type of ID was used in the input files.
     * @param chunkID
     * @return
     * @throws ClassExpressionException 
     */
    /*
    private ArrayList<EQStatement> getPredictedEQs(int chunkID) throws ClassExpressionException, Exception{
        
        ArrayList<Term> predictedQs = (ArrayList<Term>) qTermProbabilityTable.getOrDefault(chunkID, new ArrayList<>());
        ArrayList<Term> predictedEs = (ArrayList<Term>) eTermProbabilityTable.getOrDefault(chunkID, new ArrayList<>());
        
        ArrayList<Term> reducedPredictedQs = collapse(predictedQs);
        ArrayList<Term> reducedPredictedEs = collapse(predictedEs);

        // Splitting candidate PATO terms into broad categories, will not necessarily find a category for all terms.
        ArrayList<Term> reducedPredictedQsSimple = new ArrayList<>();
        ArrayList<Term> reducedPredictedQsRelational = new ArrayList<>();
        ArrayList<Term> reducedPredictedQualifiers = new ArrayList<>();
        for (Term t: reducedPredictedQs){
            if (patoInfo.relationalQualityIDs.contains(t.id)){
                reducedPredictedQsRelational.add(t);
            }
            else if (patoInfo.qualifierIDs.contains(t.id)){
                reducedPredictedQualifiers.add(t);
            }
            else {
                reducedPredictedQsSimple.add(t);
            }
        }
        

        ArrayList<EQStatement> predictedEQs = new ArrayList<>();
                
        logger.trace("using simple qualities");
        for (Term predictedQ: reducedPredictedQsSimple){
            for (Term predictedE: reducedPredictedEs){

                
                // Primary entity 1 can't be a ChEBI term.
                if (predictedE.ontology.equals(Ontology.CHEBI)){
                    break;
                }
                // If primary entity 1 is a GO:BP or GO:MF, the quality must be a child of process quality in PATO.
                if (predictedE.ontology.equals(Ontology.GO)){
                    OntologyTerm pe1 = ontoObjects.get(Ontology.GO).getTermFromTermID(predictedE.id);
                    OntologyTerm q = ontoObjects.get(Ontology.PATO).getTermFromTermID(predictedQ.id);
                    if ((pe1.allNodes.contains("GO_0008150") || pe1.allNodes.contains("GO_0003674")) && (!q.allNodes.contains("PATO_0001236"))){
                        break;
                    }
                }    
                   
                
                // Add EQ Statements in the different formats that all use non-relational qualities.
                addEQ(predictedEQs, Arrays.asList(predictedE, predictedQ), EQFormat.EQ);
                for (Term predictedQlfr: reducedPredictedQualifiers){
                    addEQ(predictedEQs, Arrays.asList(predictedE, predictedQ, predictedQlfr), EQFormat.EQq);
                    for (Term predictedPrimaryE2: reducedPredictedEs){
                        addEQ(predictedEQs, Arrays.asList(predictedE, predictedPrimaryE2, predictedQ, predictedQlfr), EQFormat.EEQq);
                    }
                }
                for (Term predictedPrimaryE2: reducedPredictedEs){
                    addEQ(predictedEQs, Arrays.asList(predictedE, predictedPrimaryE2, predictedQ), EQFormat.EEQ);
                }
                
            }
        }

        
        
        
        
        
        logger.trace("using relational qualities");
        for (Term predictedQ: reducedPredictedQsRelational){
            for (Term predictedPrE1: reducedPredictedEs){
                
                // Primary entity 1 can't be a ChEBI term.
                if (predictedPrE1.ontology.equals(Ontology.CHEBI)){
                    break;
                }
                // If primary entity 1 is a GO:BP or GO:MF, the quality must be a child of process quality in PATO.
                if (predictedPrE1.ontology.equals(Ontology.GO)){
                    OntologyTerm pe1 = ontoObjects.get(Ontology.GO).getTermFromTermID(predictedPrE1.id);
                    OntologyTerm q = ontoObjects.get(Ontology.PATO).getTermFromTermID(predictedQ.id);
                    if ((pe1.allNodes.contains("GO_0008150") || pe1.allNodes.contains("GO_0003674")) && (!q.allNodes.contains("PATO_0001236"))){
                        break;
                    }
                    
                }    
                
                
                // Add EQ Statements in the different format that all use relational qualities.
                for (Term predictedSecE1: reducedPredictedEs){
                    addEQ(predictedEQs, Arrays.asList(predictedPrE1, predictedQ, predictedSecE1), EQFormat.EQE);
                    for (Term predictedQlfr: reducedPredictedQualifiers){
                        addEQ(predictedEQs, Arrays.asList(predictedPrE1, predictedQ, predictedQlfr, predictedSecE1), EQFormat.EQqE);
                    }
                    // All the formats that add a primary entity 2.
                    for (Term predictedPrE2: reducedPredictedEs){
                        addEQ(predictedEQs, Arrays.asList(predictedPrE1, predictedPrE2, predictedQ, predictedSecE1), EQFormat.EEQE);
                        for (Term predictedQlfr: reducedPredictedQualifiers){
                            addEQ(predictedEQs, Arrays.asList(predictedPrE1, predictedPrE2, predictedQ, predictedQlfr, predictedSecE1), EQFormat.EEQqE);
                        }
                        
                        // All the formats that also add a secondary entity 2.
                        for (Term predictedSecE2: reducedPredictedEs){
                            for (Term predictedQlfr: reducedPredictedQualifiers){
                                addEQ(predictedEQs, Arrays.asList(predictedPrE1, predictedPrE2, predictedQ, predictedQlfr, predictedSecE1, predictedSecE2), EQFormat.EEQqEE);
                            }
                        }
                    }

                    // All the formats that add a secondary entity 2.
                    for (Term predictedSecE2: reducedPredictedEs){
                        addEQ(predictedEQs, Arrays.asList(predictedPrE1,  predictedQ, predictedSecE1, predictedSecE2), EQFormat.EQEE);
                        for (Term predictedQlfr: reducedPredictedQualifiers){
                            addEQ(predictedEQs, Arrays.asList(predictedPrE1, predictedQ, predictedQlfr, predictedSecE1, predictedSecE2), EQFormat.EQqEE);
                        }
                    }
                }
            }
        }

        
        // TODO set both components of the scores here instead of a mix between here and the EQ statement constructors.
        // commented out.
        for (EQStatement predictedEQ: predictedEQs){
            // predictedEQ.p1Score = p_{avg}
            
            // getting a single optimized structural probability score.
            //predictedEQ.p2Score = parser.getProbability(predictedEQ, text.getDefaultChunkFromChunkID(chunkID));     
            
            // getting multiple ones in order to test many at once.
            predictedEQ.p2Scores = parser.getProbabilities(predictedEQ, text.getDefaultChunkFromChunkID(chunkID));
            ArrayList<String> temp = new ArrayList<>();
            for (Double element: predictedEQ.p2Scores){
                temp.add(String.valueOf(element));
            }
            predictedEQ.p2ScoresString = String.join(",", temp);
            
        
        
            
        }
        
\
        return predictedEQs;
    }
    */
    
    
    
    /*
    // Checks to make sure terms aren't used twice in the EQ statement and then adds them to the candidate EQ list.
    private ArrayList<EQStatement> addEQ(ArrayList<EQStatement> predictedEQs, List<Term> terms, EQFormat format) throws Exception{
        
        HashSet<Term> termSet = new HashSet<>(terms);
        if (termSet.size() == terms.size()){
            EQStatement eq = new EQStatement(terms, format);
            predictedEQs.add(eq);
        }
        return predictedEQs;
    }
    
    
    
    
    
    
    
    
    
    
    // TODO changed this, make sure it works.
    // TODO why is the min() necessary, make sure this is doing what it's supposed to do
    private ArrayList<EQStatement> applyQThreshold(ArrayList<EQStatement> predictedEQs, double qThreshold){
        Collections.sort(predictedEQs, new EQComparatorQ());
        int lastIndexToKeep = -1;
        for (int i=0; i<predictedEQs.size(); i++){
            if (predictedEQs.get(i).coverage >= qThreshold){
                lastIndexToKeep = i;
            } 
        }    
        // Changed this, added the +1 because the second argument in sublist is the first index NOT included (exclusive).
        if (lastIndexToKeep == -1){
            
            // EDIT (if no EQ statements are greater than the threshold, keep atleast one.
            return new ArrayList<>(predictedEQs.subList(0, Math.min(1,predictedEQs.size())));
            
            // instead of returning an emtpy list.
            //return new ArrayList<>();
        }
        else {
            return new ArrayList<>(predictedEQs.subList(0, lastIndexToKeep+1));
        }
    }
    
    */
    
    
    
    
    /**
     * Takes a list of predicted terms and reduces it by removing any term T1 where some T2 exists that is a
     * descendant of T1 but where T2 has a high class probability associated with it than T1 does. In other
     * words, less specific terms are removed if and only if the list includes some more specific term that 
     * also has a higher probability of mapping to the input text.
     * @param predictedTerms
     * @return
     * @throws ClassExpressionException 
     */
    /*
    private ArrayList<Term> collapse(ArrayList<Term> predictedTerms) throws ClassExpressionException{
        
        //List of the string ontology IDs of the predicted terms.
        ArrayList<String> predictedTermIDs = new ArrayList<>();
        for (Term t: predictedTerms){
            predictedTermIDs.add(t.id);
        }
        
        
        //Remove any inherited terms that have lower probability than their descendants.
        ArrayList<String> savePredictedTermIDs = new ArrayList<>(predictedTermIDs);
        ArrayList<Term> savePredictedTerms = new ArrayList<>(predictedTerms);
        //ArrayList<Integer> indicesToDelete = new ArrayList<>();
        for (Term pt: predictedTerms){
            List<String> inheritedTermIDs = ontoObjects.get(pt.ontology).getTermFromTermID(pt.id).inheritedNodes;
            for (String inheritedNodeID: inheritedTermIDs){
                for (Term ptOther: predictedTerms){
                    if (inheritedNodeID.equals(ptOther.id) && (ptOther.probability <= pt.probability)){
                        int indexToDelete = savePredictedTermIDs.indexOf(ptOther.id);
                        if (indexToDelete != -1){
                            savePredictedTermIDs.remove(indexToDelete);
                            savePredictedTerms.remove(indexToDelete);
                        }
                        
                    }
                }
            }
        }
        return savePredictedTerms;
    }
   
   */
    
    
    /*
    private double getQScore(EQStatement predictedEQ, double beta){
        
        // Assumptions used here with the current state of the project.
        // 1. Each EQ statement must have a p1 score which is 0<=p1<=1, the average probablity of all its terms.
        // 2. Each EQ can have a p2 score 0<=p2<=1, or it can be -1 indicating that no probability could be calculated.
        //    (see the other class for reasons the probability could not be calculated)
        

        double q;
        if (predictedEQ.p2Score == -1.0){
            q = predictedEQ.p1Score;
        }
        else{
            q = ((double)beta*(double)predictedEQ.p1Score) + ((double)(1-beta)*(double)predictedEQ.p2Score);
        }
        return q;

        return 8.88;
    }
    
    
    
    // Calculate the value of q for each EQ statement.
    private ArrayList<EQStatement> assignQScores(ArrayList<EQStatement> predictedEQs, double beta){
        //Collections.sort(predictedEQs, new EQComparatorP());
        for (EQStatement eq: predictedEQs){
            eq.coverage = getQScore(eq, beta);
        }
        return predictedEQs;
    }
    
    */
    
    
   
    
    
    
    
    

    /**
     * This method takes in a file of data lines of the format (chunk ID, term ID, probability) and returns 
     * a map from chunk ID to a list of the top k matching terms as found by the classifier. The variable
     * k is not chosen here but is inferred from the file so the map simply contains every term that was 
     * specified in the file for a given chunk provided.
     * @param ontologyName
     * @param filePath
     * @return
     * @throws FileNotFoundException 
     */
    
    /*
    private HashMap readClassProbFiles(Ontology ontologyName, List<String> filePaths) throws FileNotFoundException{
        HashMap<Integer,ArrayList<Term>> chunkToTermsMap = new HashMap<>();
        for (int splitNumber=0; splitNumber<filePaths.size(); splitNumber++){
            File classProbFile = new File(filePaths.get(splitNumber));
            Scanner scanner = new Scanner(classProbFile);
            scanner.useDelimiter(",");
            // Account for the header in the class probability files.
            scanner.nextLine();
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] lineValues = line.split(",");
                // The replacement of double quotes is necessary because R scripts enclose strings in output csv files.
                String chunkIDStr = lineValues[0].replace("\"", "");
                int chunkID = Integer.valueOf(chunkIDStr);
                
                
                // New code accounting for split number inserted here.
                if (!splitMap.containsKey(chunkID)){
                    splitMap.put(chunkID, splitNumber);
                }
                
                
                
                String termID = lineValues[1].replace("\"","");
                double probability = Double.valueOf(lineValues[2]);
                
                
   
                // There has to be a better way to do this, still have to find it.
                // Adding new part here that only takes a new term if it doesn't already exist or has exists but with lower probability.
                /Update, found the better way to do this, see new code below.
                if (!chunkToTermsMap.containsKey(chunkID)){
                    chunkToTermsMap.put(chunkID, new ArrayList<>());
                    ArrayList<Term> arrayList = (ArrayList<Term>) chunkToTermsMap.get(chunkID);
                    arrayList.add(new Term(termID,probability,ontologyName));
                }
                else {
                    ArrayList<Term> arrayList = (ArrayList<Term>) chunkToTermsMap.get(chunkID);
                    arrayList.add(new Term(termID,probability,ontologyName));
                }
                /
                
 
                // New code.
                // Uses a default value to account for where no temr was previously found.
                // Check the terms that are already mapped to this chunk.
                // If this term is already found in that list, don't need to add it again but do need to update the probability.
                boolean alreadyInList = false;
                System.out.println("checking one data line.");
                for (Term term: chunkToTermsMap.getOrDefault(chunkID, new ArrayList<>())){
                    System.out.println("has existing term");
                    if (term.id.equals(termID)){
                        System.out.println("getting max and breaking");
                        term.probability = Math.max(term.probability, probability);
                        alreadyInList = true;
                        break;
                    }
                }    
                // If it wasn't in the list already, then just add it with the information from this data row.
                if (!alreadyInList){
                    System.out.println("adding new  term");
                    //System.out.println(chunkToTermsMap.getOrDefault(chunkID, new ArrayList<>()).size());
                    ArrayList<Term> listOfTerms = chunkToTermsMap.getOrDefault(chunkID, new ArrayList<>());
                    listOfTerms.add(new Term(termID,probability,ontologyName));
                    chunkToTermsMap.put(chunkID, listOfTerms);
                    //chunkToTermsMap.getOrDefault(chunkID, new ArrayList<>()).add(new Term(termID,probability,ontologyName));
                    System.out.println(chunkToTermsMap.getOrDefault(chunkID, new ArrayList<>()).size());
                }

                
            }
            // Limit the number of terms from this ontology associated with this chunk to some maximum value.
            TermComparatorByProb comparer = new TermComparatorByProb();
            for (Integer chunkID: chunkToTermsMap.keySet()){
                Collections.sort(chunkToTermsMap.get(chunkID), comparer);
                chunkToTermsMap.put(chunkID, new ArrayList<>(chunkToTermsMap.get(chunkID).subList(0, Math.min(Config.maxTermsUsedPerOntology, chunkToTermsMap.get(chunkID).size()))));           
            }
            scanner.close();
        }    
        return chunkToTermsMap;  
    }
    */

    
    /**
     * Accepts any number of maps associating chunks with lists of candidate terms, and integrates them
     * into a single map. This is used for entities because valid entities can come from a number of 
     * different ontologies but they need to be combined so a single list of valid candidate terms can
     * be found for each text chunk.
     * @param hms
     * @return 
     */
    
    /*
    private HashMap<Integer,ArrayList<Term>> mergeClassProbFiles(List<HashMap<Integer,ArrayList<Term>>> hms){
        HashMap<Integer,ArrayList<Term>> mergedChunkToTermsMap = new HashMap<>();
        HashSet<Integer> chunkIDs = new HashSet<>();
        for (HashMap<Integer,ArrayList<Term>> hm: hms){
            chunkIDs.addAll(new HashSet<>(hm.keySet()));
        }
        for (int chunkID: (HashSet<Integer>) chunkIDs){
            ArrayList<Term> terms = new ArrayList<>();
            for (HashMap<Integer,ArrayList<Term>> hm: hms){
                for (Term t: hm.getOrDefault(chunkID, new ArrayList<>())){
                    terms.add(t);
                }  
            }
            mergedChunkToTermsMap.put(chunkID, terms);
        }
        return mergedChunkToTermsMap;
    }

    */
    
    
        
}

       // potential faster version of the EQ builder stuff.

        /*
        
        
        
        // Iterate through possible qualities.
        if (predictedQualifiers.isEmpty()){
            
            
            for (Term q: predictedQsSimple){
                
                // Optional filtering steps.
                predictedEs.removeAll(Modifier.findRedundantTerms(predictedEs));            // (remove redundant entities with lower ic)
                predictedEs.removeAll(Modifier.getOverlappingTerms(q, predictedEs));        // (remove entities that overlap with the quality)
                
                // Check if a subject was only implied.
                if (hasImpliedSubject){
                    Term impliedSubjTerm = new Term("PO_0000003",1.000,Ontology.PO);
                    predictedEQs.addAll(getValidEQPermutations(impliedSubjTerm, predictedEs, q));
                }
                else{
                    predictedEQs.addAll(getValidEQPermutations(predictedEs, q));
                }
            }
            
                        // Iterate through possible quality combinations where the quality is relational.
            for (Term q: predictedQsRelational){

                    // Optional filtering steps.
                    predictedEs.removeAll(Modifier.findRedundantTerms(predictedEs));            // (remove redundant entities with lower ic)
                    predictedEs.removeAll(Modifier.getOverlappingTerms(q, predictedEs));        // (remove entities that overlap with the quality)

                    // Check if a subject was only implied.
                    if (hasImpliedSubject){
                        Term impliedSubjTerm = new Term("PO_0000003",1.000,Ontology.PO);
                        predictedEQs.addAll(getValidEQPermutations(impliedSubjTerm, predictedEs, q));
                    }
                    else{
                        predictedEQs.addAll(getValidEQPermutations(predictedEs, q));
                    }
            }
            
            
        }
        
        else{
            
            
            // Iterate through possible quality combinations where the quality is non-relational.
            for (Term q: predictedQsSimple){
                for (Term qlfr: predictedQualifiers){

                    // Optional filtering steps.
                    predictedEs.removeAll(Modifier.findRedundantTerms(predictedEs));            // (remove redundant entities with lower ic)
                    predictedEs.removeAll(Modifier.getOverlappingTerms(q, predictedEs));        // (remove entities that overlap with the quality)
                    predictedEs.removeAll(Modifier.getOverlappingTerms(qlfr, predictedEs));     // (remove entities that overlap with the qualifier)

                    // Check if a subject was only implied.
                    if (hasImpliedSubject){
                        Term impliedSubjTerm = new Term("PO_0000003",1.000,Ontology.PO);
                        predictedEQs.addAll(getValidEQPermutations(impliedSubjTerm, predictedEs, q, qlfr));
                    }
                    else{
                        predictedEQs.addAll(getValidEQPermutations(predictedEs, q, qlfr));
                    }
                }
            }



            // Iterate through possible quality combinations where the quality is relational.
            for (Term q: predictedQsRelational){
                for (Term qlfr: predictedQualifiers){

                    // Optional filtering steps.
                    predictedEs.removeAll(Modifier.findRedundantTerms(predictedEs));            // (remove redundant entities with lower ic)
                    predictedEs.removeAll(Modifier.getOverlappingTerms(q, predictedEs));        // (remove entities that overlap with the quality)
                    predictedEs.removeAll(Modifier.getOverlappingTerms(qlfr, predictedEs));     // (remove entities that overlap with the qualifier)

                    // Check if a subject was only implied.
                    if (hasImpliedSubject){
                        Term impliedSubjTerm = new Term("PO_0000003",1.000,Ontology.PO);
                        predictedEQs.addAll(getValidEQPermutations(impliedSubjTerm, predictedEs, q, qlfr));
                    }
                    else{
                        predictedEQs.addAll(getValidEQPermutations(predictedEs, q, qlfr));
                    }
                }
            }
            
            
            
        }
        
        */
       
